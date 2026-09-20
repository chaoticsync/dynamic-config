package io.dynamicconfig.core.client;

import io.dynamicconfig.core.source.MutableConfigSource;
import io.dynamicconfig.core.source.SourcePriority;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrecedenceTest {

    @Test
    void shouldApplyLowToHighPrecedence() {
        ConfigClient client = ConfigClient.builder()
                .defaults(Map.of("server.port", "8000"))
                .addSource(new MutableConfigSource("properties", Map.of("server.port", "8080")), SourcePriority.LOW)
                .addSource(new MutableConfigSource("yaml", Map.of("server.port", "9000")), SourcePriority.NORMAL)
                .addSource(new MutableConfigSource("k8s", Map.of("server.port", "10000")), SourcePriority.HIGHEST)
                .build();

        client.refresh();

        assertEquals("10000", client.get("server.port"));
    }

}
