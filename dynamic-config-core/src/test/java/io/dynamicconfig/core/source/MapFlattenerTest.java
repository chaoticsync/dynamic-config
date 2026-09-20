package io.dynamicconfig.core.source;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapFlattenerTest {

    @Test
    void shouldFlattenNestedMap() {

        Map<String, Object> input = Map.of(
                "database",
                Map.of(
                        "host", "localhost",
                        "port", 3306
                )
        );

        Map<String, String> flat = MapFlattener.flatten(input);

        assertEquals("localhost", flat.get("database.host"));
        assertEquals("3306", flat.get("database.port"));
    }

    @Test
    void shouldFlattenList() {

        Map<String, Object> input = Map.of(
                "servers",
                java.util.List.of("app1", "app2")
        );

        Map<String, String> flat = MapFlattener.flatten(input);

        assertEquals("app1", flat.get("servers[0]"));
        assertEquals("app2", flat.get("servers[1]"));
    }
}