package io.dynamicconfig.core.client;

import io.dynamicconfig.core.source.MutableConfigSource;
import io.dynamicconfig.core.source.SourcePriority;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ShutdownTest {

    @Test
    void shutdownShouldBeIdempotent() {
        ConfigClient client = ConfigClient.builder()
                .addSource(new MutableConfigSource("test", Map.of("key", "value")))
                .build();

        client.start();
        client.shutdown();
        client.shutdown();

        assertEquals("value", client.get("key"));
    }

    @Test
    void refreshAndBackgroundStartAreNoOpsAfterShutdown() {
        MutableConfigSource source = new MutableConfigSource("test", Map.of("key", "value"));

        ConfigClient client = ConfigClient.builder()
                .addSource(source)
                .refreshInterval(Duration.ofMillis(50))
                .build();

        client.start();
        client.shutdown();

        source.set("key", "changed");
        client.refresh();
        client.startBackgroundLifecycle();

        assertEquals("value", client.get("key"));
    }

    @Test
    void watchRegistrationIsIgnoredAfterShutdown() {
        ConfigClient client = ConfigClient.builder()
                .addSource(new MutableConfigSource("test", Map.of("key", "value")))
                .build();

        client.start();
        client.shutdown();

        AtomicBoolean invoked = new AtomicBoolean(false);
        client.watch("key", (key, oldValue, newValue) -> invoked.set(true));

        assertFalse(invoked.get());
    }

}
