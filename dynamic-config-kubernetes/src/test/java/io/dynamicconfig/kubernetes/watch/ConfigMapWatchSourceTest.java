package io.dynamicconfig.kubernetes.watch;

import io.dynamicconfig.core.source.RefreshTrigger;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.Watch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigMapWatchSourceTest {

    private ConfigMapWatchSource source;

    @AfterEach
    void tearDown() {
        if (source != null) {
            source.stop();
        }
    }

    @Test
    void shouldLoadInitialConfigMapOnStart() throws Exception {
        FakeConfigMapApiClient apiClient = new FakeConfigMapApiClient();
        apiClient.setConfigMap(configMap("1", Map.of(
                "application.yaml",
                "server:\n  port: 8080\n"
        )));

        source = new ConfigMapWatchSource("default", "app-config", apiClient);
        source.start(() -> {});

        Map<String, String> loaded = source.load();
        assertEquals("8080", loaded.get("server.port"));
    }

    @Test
    void shouldReturnDefensiveCopyFromLoad() throws Exception {
        FakeConfigMapApiClient apiClient = new FakeConfigMapApiClient();
        apiClient.setConfigMap(configMap("1", Map.of(
                "application.properties",
                "server.port=8080\n"
        )));

        source = new ConfigMapWatchSource("default", "app-config", apiClient);
        source.start(() -> {});

        Map<String, String> first = source.load();
        Map<String, String> second = source.load();

        assertEquals(first, second);
        assertNotSame(first, second);
    }

    @Test
    void shouldTriggerRefreshOnWatchEvent() throws Exception {
        FakeConfigMapApiClient apiClient = new FakeConfigMapApiClient();
        apiClient.setConfigMap(configMap("1", Map.of(
                "application.properties",
                "server.port=8080\n"
        )));

        CountDownLatch refreshLatch = new CountDownLatch(1);
        source = new ConfigMapWatchSource("default", "app-config", apiClient);
        source.start(refreshLatch::countDown);

        apiClient.emitWatchEvent(new Watch.Response<>(
                "MODIFIED",
                configMap("2", Map.of(
                        "application.properties",
                        "server.port=9090\n"
                ))
        ));

        assertTrue(refreshLatch.await(2, TimeUnit.SECONDS));
        assertEquals("9090", source.load().get("server.port"));
    }

    @Test
    void shouldReconnectAfterWatchFailure() throws Exception {
        FakeConfigMapApiClient apiClient = new FakeConfigMapApiClient();
        apiClient.setConfigMap(configMap("1", Map.of(
                "application.properties",
                "server.port=8080\n"
        )));
        apiClient.failNextWatchWith(new ApiException("watch failed"));

        AtomicInteger refreshCount = new AtomicInteger();
        source = new ConfigMapWatchSource("default", "app-config", apiClient);
        source.start(refreshCount::incrementAndGet);

        apiClient.emitWatchEvent(new Watch.Response<>(
                "MODIFIED",
                configMap("2", Map.of(
                        "application.properties",
                        "server.port=7070\n"
                ))
        ));

        Thread.sleep(1500);
        assertEquals("7070", source.load().get("server.port"));
    }

    private static V1ConfigMap configMap(String resourceVersion, Map<String, String> data) {
        V1ConfigMap configMap = new V1ConfigMap();
        configMap.setMetadata(new V1ObjectMeta().resourceVersion(resourceVersion));
        configMap.setData(new LinkedHashMap<>(data));
        return configMap;
    }

    private static final class FakeConfigMapApiClient implements ConfigMapApiClient {

        private V1ConfigMap configMap;
        private final List<Watch.Response<V1ConfigMap>> watchEvents = new ArrayList<>();
        private ApiException nextWatchFailure;
        private volatile boolean watchOpen;

        void setConfigMap(V1ConfigMap configMap) {
            this.configMap = configMap;
        }

        void failNextWatchWith(ApiException failure) {
            this.nextWatchFailure = failure;
        }

        void emitWatchEvent(Watch.Response<V1ConfigMap> event) throws InterruptedException {
            while (!watchOpen) {
                Thread.sleep(10);
            }
            watchEvents.add(event);
        }

        @Override
        public V1ConfigMap getConfigMap(String namespace, String name) {
            return configMap;
        }

        @Override
        public ConfigMapWatch openWatch(String namespace, String name, String resourceVersion) throws ApiException {
            if (nextWatchFailure != null) {
                ApiException failure = nextWatchFailure;
                nextWatchFailure = null;
                throw failure;
            }
            return new FakeConfigMapWatch(watchEvents);
        }

        private final class FakeConfigMapWatch implements ConfigMapWatch {

            private final List<Watch.Response<V1ConfigMap>> events;
            private int index;

            private FakeConfigMapWatch(List<Watch.Response<V1ConfigMap>> events) {
                this.events = events;
                watchOpen = true;
            }

            @Override
            public boolean hasNext() {
                while (index >= events.size()) {
                    if (!watchOpen) {
                        return false;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
                return true;
            }

            @Override
            public Watch.Response<V1ConfigMap> next() {
                return events.get(index++);
            }

            @Override
            public void close() {
                watchOpen = false;
            }

        }

    }

}
