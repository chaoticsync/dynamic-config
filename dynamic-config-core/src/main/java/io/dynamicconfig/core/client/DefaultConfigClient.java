package io.dynamicconfig.core.client;

import io.dynamicconfig.core.cache.ConfigCache;
import io.dynamicconfig.core.cache.InMemoryConfigCache;
import io.dynamicconfig.core.source.ConfigSource;
import io.dynamicconfig.core.watch.ConfigWatcher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultConfigClient
        implements ConfigClient {

    private final List<ConfigSource> sources;

    private final ConfigCache cache =
            new InMemoryConfigCache();

    private final Map<String,
            List<ConfigWatcher>>
            watchers =
            new ConcurrentHashMap<>();

    public DefaultConfigClient(
            List<ConfigSource> sources) {

        this.sources = sources;
    }

    @Override
    public String get(String key) {

        return cache.get(key);
    }

    @Override
    public Integer getInt(String key) {

        String value = get(key);

        return value == null
                ? null
                : Integer.parseInt(value);
    }

    @Override
    public Long getLong(String key) {

        String value = get(key);

        return value == null
                ? null
                : Long.parseLong(value);
    }

    @Override
    public Double getDouble(String key) {

        String value = get(key);

        return value == null
                ? null
                : Double.parseDouble(value);
    }

    @Override
    public Boolean getBoolean(String key) {

        String value = get(key);

        return value == null
                ? null
                : Boolean.parseBoolean(value);
    }

    @Override
    public void watch(
            String key,
            ConfigWatcher watcher) {

        watchers.computeIfAbsent(
                        key,
                        k -> new ArrayList<>())
                .add(watcher);
    }

    @Override
    public void refresh() {

        Map<String, String> oldSnapshot =
                cache.snapshot();

        Map<String, String> merged =
                new LinkedHashMap<>();

        for (ConfigSource source : sources) {

            try {

                source.load()
                        .forEach(merged::putIfAbsent);

            } catch (Exception ex) {

                System.err.println(
                        "Failed loading source: "
                                + source.getName());

                ex.printStackTrace();
            }
        }

        cache.replace(merged);

        detectChanges(
                oldSnapshot,
                merged);
    }

    private void detectChanges(
            Map<String, String> oldMap,
            Map<String, String> newMap) {

        newMap.forEach((key, value) -> {

            String oldValue =
                    oldMap.get(key);

            if (!Objects.equals(
                    oldValue,
                    value)) {

                notifyWatchers(
                        key,
                        oldValue,
                        value);
            }
        });
    }

    private void notifyWatchers(
            String key,
            String oldValue,
            String newValue) {

        List<ConfigWatcher> watcherList =
                watchers.get(key);

        if (watcherList == null) {
            return;
        }

        watcherList.forEach(
                watcher ->
                        watcher.onChange(
                                key,
                                oldValue,
                                newValue));
    }

    @Override
    public void start() {

        refresh();
    }

    @Override
    public void shutdown() {
    }
}