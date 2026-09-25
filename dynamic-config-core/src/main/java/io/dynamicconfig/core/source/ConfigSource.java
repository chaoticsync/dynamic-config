package io.dynamicconfig.core.source;

import java.util.Map;

/**
 * Represents a source of configuration values.
 *
 * <p>Implementations load configuration from a specific location such as
 * Properties files, YAML files, JSON files, Kubernetes ConfigMaps, or
 * external configuration services.
 *
 * <p>Returned configuration must be flattened into key-value pairs.
 */
public interface ConfigSource {

    /**
     * Human readable source name.
     */
    String getName();

    /**
     * Loads configuration as a flattened map.
     */
    Map<String, String> load();

}