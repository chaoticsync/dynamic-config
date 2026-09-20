package io.dynamicconfig.core.watch;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ChangeDetector {

    @FunctionalInterface
    public interface ChangeListener {

        void onChange(String key, String oldValue, String newValue);

    }

    public void detectChanges(Map<String, String> oldMap, Map<String, String> newMap, ChangeListener listener) {
        Set<String> keys = new HashSet<>();
        keys.addAll(oldMap.keySet());
        keys.addAll(newMap.keySet());

        for (String key : keys) {
            String oldValue = oldMap.get(key);
            String newValue = newMap.get(key);

            if (!Objects.equals(oldValue, newValue)) {
                listener.onChange(key, oldValue, newValue);
            }
        }
    }

}
