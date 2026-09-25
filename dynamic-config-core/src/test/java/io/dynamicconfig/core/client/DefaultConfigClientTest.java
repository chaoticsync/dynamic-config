package io.dynamicconfig.core.client;

import io.dynamicconfig.core.source.ConfigSource;
import io.dynamicconfig.core.source.MutableConfigSource;
import io.dynamicconfig.core.source.SourcePriority;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DefaultConfigClientTest {

    private ConfigSource source(Map<String, String> values) {
        return new ConfigSource() {
            @Override
            public String getName() {
                return "test-source";
            }

            @Override
            public Map<String, String> load() {
                return values;
            }
        };
    }

    @Test
    void shouldLoadConfiguration() {
        ConfigClient client = ConfigClient.builder()
                .addSource(source(Map.of(
                        "server.port", "8080",
                        "feature.enabled", "true"
                )))
                .build();

        client.start();

        assertEquals("8080", client.get("server.port"));
        assertEquals("true", client.get("feature.enabled"));
    }

    @Test
    void shouldReturnTypedValues() {
        ConfigClient client = ConfigClient.builder()
                .addSource(source(Map.of(
                        "port", "8080",
                        "enabled", "true",
                        "timeout", "5000",
                        "ratio", "0.75"
                )))
                .build();

        client.start();

        assertEquals(8080, client.getInt("port"));
        assertEquals(true, client.getBoolean("enabled"));
        assertEquals(5000L, client.getLong("timeout"));
        assertEquals(0.75, client.getDouble("ratio"));
    }

    @Test
    void shouldReturnDefaultValues() {
        ConfigClient client = ConfigClient.builder()
                .addSource(source(Map.of()))
                .build();

        client.start();

        assertEquals("localhost", client.get("host", "localhost"));
        assertEquals(8080, client.getInt("port", 8080));
        assertEquals(true, client.getBoolean("enabled", true));
    }

    @Test
    void shouldUseHighestPrioritySourceWhenDuplicateKeysExist() {
        ConfigClient client = ConfigClient.builder()
                .addSource(source(Map.of("server.port", "8080")), SourcePriority.LOW)
                .addSource(source(Map.of("server.port", "9090")), SourcePriority.HIGH)
                .build();

        client.start();

        assertEquals("9090", client.get("server.port"));
    }

    @Test
    void shouldCheckIfKeyExists() {
        ConfigClient client = ConfigClient.builder()
                .addSource(source(Map.of("host", "localhost")))
                .build();

        client.start();

        assertTrue(client.contains("host"));
        assertFalse(client.contains("missing"));
    }

    @Test
    void shouldReturnSnapshot() {
        ConfigClient client = ConfigClient.builder()
                .addSource(source(Map.of(
                        "host", "localhost",
                        "port", "8080"
                )))
                .build();

        client.start();

        Map<String, String> snapshot = client.snapshot();

        assertEquals(2, snapshot.size());
        assertEquals("localhost", snapshot.get("host"));
        assertEquals("8080", snapshot.get("port"));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.put("x", "y"));
    }

    @Test
    void shouldNotifyWatcherOnChange() {
        MutableConfigSource mutableSource = new MutableConfigSource("mutable", Map.of("host", "localhost"));

        ConfigClient client = ConfigClient.builder()
                .addSource(mutableSource)
                .build();

        AtomicReference<String> oldValue = new AtomicReference<>();
        AtomicReference<String> newValue = new AtomicReference<>();

        client.watch("host", (key, oldVal, newVal) -> {
            oldValue.set(oldVal);
            newValue.set(newVal);
        });

        client.start();

        mutableSource.set("host", "127.0.0.1");
        client.refresh();

        assertEquals("localhost", oldValue.get());
        assertEquals("127.0.0.1", newValue.get());
    }

}
