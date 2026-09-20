package io.dynamicconfig.core.watch;

/**
 * Callback interface notified when a configuration value changes.
 */

@FunctionalInterface
public interface ConfigWatcher {

    void onChange(
            String key,
            String oldValue,
            String newValue);
}