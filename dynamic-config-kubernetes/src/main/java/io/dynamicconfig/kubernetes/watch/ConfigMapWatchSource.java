package io.dynamicconfig.kubernetes.watch;

import io.dynamicconfig.core.exception.ConfigLoadException;
import io.dynamicconfig.core.source.LifecycleConfigSource;
import io.dynamicconfig.core.source.RefreshTrigger;
import io.dynamicconfig.kubernetes.internal.ConfigMapDataParser;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.Watch;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Watches a Kubernetes ConfigMap through the API, keeps an in-memory snapshot,
 * and triggers refreshes when the resource changes.
 */
public final class ConfigMapWatchSource implements LifecycleConfigSource {

    private static final Logger LOGGER = Logger.getLogger(ConfigMapWatchSource.class.getName());
    private static final Duration RECONNECT_DELAY = Duration.ofSeconds(1);

    private final String namespace;
    private final String name;
    private final ConfigMapApiClient apiClient;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, String> state = new LinkedHashMap<>();
    private final AtomicReference<RefreshTrigger> refreshTrigger = new AtomicReference<>();

    private volatile boolean running;
    private volatile Thread watchThread;

    public ConfigMapWatchSource(String namespace, String name) {
        this(namespace, name, createDefaultApiClient());
    }

    ConfigMapWatchSource(String namespace, String name, ConfigMapApiClient apiClient) {
        this.namespace = Objects.requireNonNull(namespace, "namespace cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient cannot be null");
    }

    private static ConfigMapApiClient createDefaultApiClient() {
        try {
            return DefaultConfigMapApiClient.fromDefaultClient();
        } catch (Exception ex) {
            throw new ConfigLoadException("Unable to create Kubernetes API client", ex);
        }
    }

    @Override
    public String getName() {
        return "configmap-watch:" + namespace + "/" + name;
    }

    @Override
    public Map<String, String> load() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableMap(new LinkedHashMap<>(state));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void start(RefreshTrigger refreshTrigger) {
        Objects.requireNonNull(refreshTrigger, "refreshTrigger cannot be null");
        this.refreshTrigger.set(refreshTrigger);
        this.running = true;

        try {
            V1ConfigMap configMap = apiClient.getConfigMap(namespace, name);
            replaceState(ConfigMapDataParser.parseData(configMap.getData()));
        } catch (ApiException ex) {
            throw new ConfigLoadException(
                    "Unable to load ConfigMap " + namespace + "/" + name,
                    ex
            );
        }

        watchThread = new Thread(this::watchLoop, "configmap-watch-" + namespace + "-" + name);
        watchThread.setDaemon(true);
        watchThread.start();
    }

    @Override
    public void stop() {
        running = false;
        Thread thread = watchThread;
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(RECONNECT_DELAY.toMillis());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        refreshTrigger.set(null);
    }

    private void watchLoop() {
        String resourceVersion = null;

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                V1ConfigMap current = apiClient.getConfigMap(namespace, name);
                resourceVersion = extractResourceVersion(current);

                try (ConfigMapWatch watch = apiClient.openWatch(namespace, name, resourceVersion)) {
                    while (running && watch.hasNext()) {
                        Watch.Response<V1ConfigMap> event = watch.next();
                        handleWatchEvent(event);
                        if (event.object != null) {
                            resourceVersion = extractResourceVersion(event.object);
                        }
                    }
                }
            } catch (Exception ex) {
                if (!running || Thread.currentThread().isInterrupted()) {
                    return;
                }
                LOGGER.log(Level.WARNING, "ConfigMap watch failed for " + namespace + "/" + name + ", reconnecting", ex);
                try {
                    sleepBeforeReconnect();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void handleWatchEvent(Watch.Response<V1ConfigMap> event) {
        if (event == null) {
            return;
        }

        if ("DELETED".equals(event.type)) {
            replaceState(Map.of());
            triggerRefresh();
            return;
        }

        V1ConfigMap configMap = event.object;
        if (configMap == null) {
            return;
        }

        replaceState(ConfigMapDataParser.parseData(configMap.getData()));
        triggerRefresh();
    }

    private void replaceState(Map<String, String> newState) {
        lock.writeLock().lock();
        try {
            state.clear();
            state.putAll(newState);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void triggerRefresh() {
        RefreshTrigger trigger = refreshTrigger.get();
        if (trigger != null) {
            trigger.refresh();
        }
    }

    private static String extractResourceVersion(V1ConfigMap configMap) {
        V1ObjectMeta metadata = configMap.getMetadata();
        return metadata == null ? null : metadata.getResourceVersion();
    }

    private void sleepBeforeReconnect() throws InterruptedException {
        Thread.sleep(RECONNECT_DELAY.toMillis());
    }

}
