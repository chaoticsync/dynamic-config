package io.dynamicconfig.core.source;

import java.util.Map;

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