package io.dynamicconfig.core.client;

import io.dynamicconfig.core.exception.ConfigConversionException;
import io.dynamicconfig.core.watch.ConfigWatcher;

import java.util.Map;

/**
 * Primary entry point for accessing configuration values.
 */
public interface ConfigClient {

    Map<String, String> snapshot();

    String get(String key);

    String get(String key, String defaultValue);

    Integer getInt(String key);

    Integer getInt(String key, Integer defaultValue);

    Long getLong(String key);

    Long getLong(String key, Long defaultValue);

    Double getDouble(String key);

    Double getDouble(String key, Double defaultValue);

    Boolean getBoolean(String key);

    Boolean getBoolean(String key, Boolean defaultValue);

    boolean contains(String key);

    void watch(String key, ConfigWatcher watcher);

    /**
     * Reloads configuration from all registered sources, updates the cache, and notifies watchers.
     *
     * <p>Watcher callbacks must not synchronously re-enter {@code refresh()} on the same client.
     */
    void refresh();

    /**
     * Starts lifecycle sources, refreshes once, then starts the background refresh strategy.
     *
     * <p>Idempotent — subsequent calls are no-ops.
     */
    void startBackgroundLifecycle();

    /**
     * Convenience for non-Spring usage: {@code refresh()} then {@code startBackgroundLifecycle()}.
     */
    void start();

    void shutdown();

    static ConfigClientBuilder builder() {
        return new ConfigClientBuilder();
    }

}
