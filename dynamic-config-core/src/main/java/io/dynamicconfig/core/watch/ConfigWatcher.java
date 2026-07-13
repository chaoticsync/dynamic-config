package io.dynamicconfig.core.watch;

@FunctionalInterface
public interface ConfigWatcher {

    void onChange(
            String key,
            String oldValue,
            String newValue);
}