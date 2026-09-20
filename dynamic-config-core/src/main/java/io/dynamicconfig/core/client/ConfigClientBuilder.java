package io.dynamicconfig.core.client;

import io.dynamicconfig.core.source.ConfigSource;
import io.dynamicconfig.core.source.DefaultsConfigSource;
import io.dynamicconfig.core.source.SourcePriority;
import io.dynamicconfig.core.source.SourceRegistration;
import io.dynamicconfig.core.source.file.JsonConfigSource;
import io.dynamicconfig.core.source.file.PropertiesConfigSource;
import io.dynamicconfig.core.source.file.YamlConfigSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ConfigClientBuilder {

    private final List<SourceRegistration> registrations = new ArrayList<>();
    private int nextRegistrationOrder;
    private Duration refreshInterval;

    public static ConfigClientBuilder builder() {
        return new ConfigClientBuilder();
    }

    public ConfigClientBuilder defaults(Map<String, String> defaults) {
        Objects.requireNonNull(defaults, "defaults cannot be null");
        if (!defaults.isEmpty()) {
            addSource(new DefaultsConfigSource(defaults), SourcePriority.LOWEST);
        }
        return this;
    }

    public ConfigClientBuilder properties(String file) {
        return addSource(new PropertiesConfigSource(file), SourcePriority.LOW);
    }

    public ConfigClientBuilder yaml(String file) {
        return addSource(new YamlConfigSource(file), SourcePriority.NORMAL);
    }

    public ConfigClientBuilder json(String file) {
        return addSource(new JsonConfigSource(file), SourcePriority.NORMAL);
    }

    public ConfigClientBuilder addSource(ConfigSource source) {
        return addSource(source, SourcePriority.NORMAL);
    }

    public ConfigClientBuilder addSource(ConfigSource source, SourcePriority priority) {
        Objects.requireNonNull(source, "ConfigSource cannot be null");
        Objects.requireNonNull(priority, "SourcePriority cannot be null");
        registrations.add(new SourceRegistration(source, priority, nextRegistrationOrder++));
        return this;
    }

    public ConfigClientBuilder refreshInterval(Duration interval) {
        this.refreshInterval = interval;
        return this;
    }

    public ConfigClient build() {
        if (registrations.isEmpty()) {
            throw new IllegalStateException("At least one ConfigSource must be registered.");
        }
        return new DefaultConfigClient(List.copyOf(registrations), refreshInterval);
    }

}
