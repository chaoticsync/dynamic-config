package io.dynamicconfig.core.client;

import io.dynamicconfig.core.source.ConfigSource;

import java.util.ArrayList;
import java.util.List;

public class ConfigClientBuilder {

    private final List<ConfigSource> sources = new ArrayList<>();

    public ConfigClientBuilder addSource(ConfigSource source) {
        sources.add(source);
        return this;
    }

    public ConfigClient build() {

        return new DefaultConfigClient(sources);
    }
}