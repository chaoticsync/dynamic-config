package io.dynamicconfig.core.client;

import io.dynamicconfig.core.cache.ConfigCache;
import io.dynamicconfig.core.cache.InMemoryConfigCache;
import io.dynamicconfig.core.conversion.ConfigValueConverter;
import io.dynamicconfig.core.refresh.NoOpRefreshStrategy;
import io.dynamicconfig.core.refresh.PollingRefreshStrategy;
import io.dynamicconfig.core.refresh.RefreshStrategy;
import io.dynamicconfig.core.source.ConfigurationMerger;
import io.dynamicconfig.core.source.LifecycleConfigSource;
import io.dynamicconfig.core.source.RefreshTrigger;
import io.dynamicconfig.core.source.SourceRegistration;
import io.dynamicconfig.core.watch.ChangeDetector;
import io.dynamicconfig.core.watch.ConfigWatcher;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DefaultConfigClient implements ConfigClient {

    private static final Logger LOGGER = Logger.getLogger(DefaultConfigClient.class.getName());

    private final List<SourceRegistration> sources;
    private final List<LifecycleConfigSource> lifecycleSources;
    private final RefreshStrategy refreshStrategy;
    private final ConfigurationMerger merger = new ConfigurationMerger();
    private final ChangeDetector changeDetector = new ChangeDetector();
    private final ConfigCache cache = new InMemoryConfigCache();
    private final Map<String, List<ConfigWatcher>> watchers = new java.util.concurrent.ConcurrentHashMap<>();
    private final ReentrantLock refreshLock = new ReentrantLock();
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicBoolean backgroundStarted = new AtomicBoolean(false);

    DefaultConfigClient(List<SourceRegistration> sources, Duration refreshInterval) {
        this.sources = List.copyOf(sources);
        this.lifecycleSources = sources.stream()
                .map(SourceRegistration::getSource)
                .filter(LifecycleConfigSource.class::isInstance)
                .map(LifecycleConfigSource.class::cast)
                .toList();
        if (refreshInterval != null && !refreshInterval.isZero() && !refreshInterval.isNegative()) {
            this.refreshStrategy = new PollingRefreshStrategy(this::refresh, refreshInterval);
        } else {
            this.refreshStrategy = new NoOpRefreshStrategy();
        }
    }

    @Override
    public Map<String, String> snapshot() {
        return cache.snapshot();
    }

    @Override
    public String get(String key) {
        return cache.get(key);
    }

    @Override
    public String get(String key, String defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public Integer getInt(String key) {
        return ConfigValueConverter.toInteger(key, get(key));
    }

    @Override
    public Integer getInt(String key, Integer defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : ConfigValueConverter.toInteger(key, value);
    }

    @Override
    public Long getLong(String key) {
        return ConfigValueConverter.toLong(key, get(key));
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : ConfigValueConverter.toLong(key, value);
    }

    @Override
    public Double getDouble(String key) {
        return ConfigValueConverter.toDouble(key, get(key));
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : ConfigValueConverter.toDouble(key, value);
    }

    @Override
    public Boolean getBoolean(String key) {
        return ConfigValueConverter.toBoolean(key, get(key));
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : ConfigValueConverter.toBoolean(key, value);
    }

    @Override
    public boolean contains(String key) {
        return cache.contains(key);
    }

    @Override
    public void watch(String key, ConfigWatcher watcher) {
        if (shutdown.get()) {
            return;
        }
        watchers.computeIfAbsent(key, ignored -> new CopyOnWriteArrayList<>()).add(watcher);
    }

    @Override
    public void refresh() {
        if (shutdown.get()) {
            return;
        }

        refreshLock.lock();
        Map<String, String> oldSnapshot;
        Map<String, String> merged;
        try {
            oldSnapshot = Map.copyOf(cache.snapshot());
            merged = merger.merge(sources);
            cache.replace(merged);
        } finally {
            refreshLock.unlock();
        }

        changeDetector.detectChanges(oldSnapshot, merged, this::notifyWatchers);
    }

    @Override
    public void startBackgroundLifecycle() {
        if (shutdown.get() || !backgroundStarted.compareAndSet(false, true)) {
            return;
        }

        RefreshTrigger trigger = () -> refresh();

        for (LifecycleConfigSource source : lifecycleSources) {
            try {
                source.start(trigger);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed to start lifecycle source: " + source.getName(), ex);
            }
        }

        refresh();
        refreshStrategy.start();
    }

    @Override
    public void start() {
        refresh();
        startBackgroundLifecycle();
    }

    @Override
    public void shutdown() {
        if (!shutdown.compareAndSet(false, true)) {
            return;
        }

        refreshStrategy.stop();

        for (LifecycleConfigSource source : lifecycleSources) {
            try {
                source.stop();
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed to stop lifecycle source: " + source.getName(), ex);
            }
        }
    }

    private void notifyWatchers(String key, String oldValue, String newValue) {
        List<ConfigWatcher> watcherList = watchers.get(key);
        if (watcherList == null || watcherList.isEmpty()) {
            return;
        }

        List<ConfigWatcher> snapshot = List.copyOf(watcherList);
        for (ConfigWatcher watcher : snapshot) {
            try {
                watcher.onChange(key, oldValue, newValue);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Watcher failed for key: " + key, ex);
            }
        }
    }

}
