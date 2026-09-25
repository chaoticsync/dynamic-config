package io.dynamicconfig.core.source;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In-memory configuration source primarily useful for testing and programmatic updates.
 */
public final class MutableConfigSource implements ConfigSource {

    private final String name;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, String> values = new LinkedHashMap<>();

    public MutableConfigSource(String name) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
    }

    public MutableConfigSource(String name, Map<String, String> initialValues) {
        this(name);
        if (initialValues != null) {
            values.putAll(initialValues);
        }
    }

    public void set(String key, String value) {
        lock.writeLock().lock();
        try {
            if (value == null) {
                values.remove(key);
            } else {
                values.put(key, value);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, String> load() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableMap(new LinkedHashMap<>(values));
        } finally {
            lock.readLock().unlock();
        }
    }

}
