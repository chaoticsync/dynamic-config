package io.dynamicconfig.core.source;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConfigurationMerger {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationMerger.class.getName());

    public Map<String, String> merge(List<SourceRegistration> sources) {
        List<SourceRegistration> ordered = sources.stream()
                .sorted(Comparator
                        .comparingInt((SourceRegistration registration) -> registration.getPriority().ordinal())
                        .thenComparingInt(SourceRegistration::getRegistrationOrder))
                .toList();

        Map<String, String> merged = new LinkedHashMap<>();

        for (SourceRegistration registration : ordered) {
            ConfigSource source = registration.getSource();
            try {
                source.load().forEach(merged::put);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed loading source: " + source.getName(), ex);
            }
        }

        return Map.copyOf(merged);
    }

}
