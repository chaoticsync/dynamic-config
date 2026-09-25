package io.dynamicconfig.core.source;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationMergerTest {

    @Test
    void shouldMergeByPriorityLowToHigh() {
        ConfigurationMerger merger = new ConfigurationMerger();

        List<SourceRegistration> sources = List.of(
                new SourceRegistration(namedSource("defaults", Map.of("server.port", "8000")), SourcePriority.LOWEST, 0),
                new SourceRegistration(namedSource("properties", Map.of("server.port", "8080")), SourcePriority.LOW, 1),
                new SourceRegistration(namedSource("yaml", Map.of("server.port", "9090")), SourcePriority.NORMAL, 2));

        Map<String, String> merged = merger.merge(sources);

        assertEquals("9090", merged.get("server.port"));
    }

    @Test
    void shouldPreferLaterRegistrationAtSamePriority() {
        ConfigurationMerger merger = new ConfigurationMerger();

        List<SourceRegistration> sources = List.of(
                new SourceRegistration(namedSource("first", Map.of("key", "first")), SourcePriority.NORMAL, 0),
                new SourceRegistration(namedSource("second", Map.of("key", "second")), SourcePriority.NORMAL, 1));

        Map<String, String> merged = merger.merge(sources);

        assertEquals("second", merged.get("key"));
    }

    @Test
    void shouldSkipFailedSourceAndKeepLowerPriorityValues() {
        ConfigurationMerger merger = new ConfigurationMerger();

        List<SourceRegistration> sources = List.of(
                new SourceRegistration(namedSource("low", Map.of("server.port", "8080")), SourcePriority.LOW, 0),
                new SourceRegistration(failingSource("high"), SourcePriority.HIGHEST, 1));

        Map<String, String> merged = merger.merge(sources);

        assertEquals("8080", merged.get("server.port"));
    }

    private static ConfigSource namedSource(String name, Map<String, String> values) {
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

    private static ConfigSource failingSource(String name) {
        return new ConfigSource() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Map<String, String> load() {
                throw new RuntimeException("boom");
            }
        };
    }

}
