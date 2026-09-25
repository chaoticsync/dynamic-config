package io.dynamicconfig.core.client;

import io.dynamicconfig.core.source.ConfigSource;
import io.dynamicconfig.core.source.SourcePriority;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigClientBuilderTest {

    @Test
    void shouldRejectEmptySources() {
        assertThrows(IllegalStateException.class, () -> ConfigClient.builder().build());
    }

    @Test
    void shouldRegisterDefaultsAtLowestPriority() {
        ConfigClient client = ConfigClient.builder()
                .defaults(Map.of("key", "default"))
                .addSource(source("override", Map.of("key", "override")), SourcePriority.NORMAL)
                .build();

        client.refresh();

        assertEquals("override", client.get("key"));
    }

    private static ConfigSource source(String name, Map<String, String> values) {
        return new ConfigSource() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Map<String, String> load() {
                return values;
            }
        };
    }

}
