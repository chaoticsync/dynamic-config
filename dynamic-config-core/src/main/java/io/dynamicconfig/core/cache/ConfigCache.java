package io.dynamicconfig.core.cache;

import java.util.Map;

/**
 * Stores flattened configuration values in memory.
 *
 * Implementations are responsible for providing thread-safe access to
 * configuration entries.
 */
public interface ConfigCache {

    String get(String key);

    boolean contains(String key);

    Map<String, String> snapshot();

    void replace(Map<String, String> newValues);

    void clear();

}