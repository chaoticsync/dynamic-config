package io.dynamicconfig.core.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class InMemoryConfigCache implements ConfigCache {

    private final AtomicReference<Map<String, String>> cache = new AtomicReference<>(Map.of());

    @Override
    public String get(String key) {
        return cache.get().get(key);
    }

    @Override
    public boolean contains(String key) {
        return cache.get().containsKey(key);
    }

    @Override
    public Map<String, String> snapshot() {
        return cache.get();
    }

    @Override
    public void replace(Map<String, String> data) {
        cache.set(Collections.unmodifiableMap(new HashMap<>(data)));
    }

    @Override
    public void clear() {
        cache.set(Map.of());
    }
}