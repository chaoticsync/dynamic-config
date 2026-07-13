package io.dynamicconfig.core.client;

import io.dynamicconfig.core.watch.ConfigWatcher;

public interface ConfigClient {

    String get(String key);

    Integer getInt(String key);

    Long getLong(String key);

    Double getDouble(String key);

    Boolean getBoolean(String key);

    void watch(
            String key,
            ConfigWatcher watcher);

    void refresh();

    void start();

    void shutdown();
}