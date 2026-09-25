package io.dynamicconfig.kubernetes.source;

import io.dynamicconfig.core.exception.ConfigValidationException;
import io.dynamicconfig.core.source.ConfigSource;
import io.dynamicconfig.core.source.file.JsonConfigSource;
import io.dynamicconfig.core.source.file.PropertiesConfigSource;
import io.dynamicconfig.core.source.file.YamlConfigSource;

import java.util.Map;
import java.util.Objects;

/**
 * Reads configuration from a single Kubernetes-mounted ConfigMap file and
 * delegates parsing to the appropriate core file source based on extension.
 */
public final class ConfigMapFileSource implements ConfigSource {

    private final ConfigSource delegate;

    public ConfigMapFileSource(String filePath) {
        Objects.requireNonNull(filePath, "filePath cannot be null");
        this.delegate = createDelegate(filePath);
    }

    private static ConfigSource createDelegate(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".yaml") || lowerPath.endsWith(".yml")) {
            return new YamlConfigSource(filePath);
        }
        if (lowerPath.endsWith(".json")) {
            return new JsonConfigSource(filePath);
        }
        if (lowerPath.endsWith(".properties")) {
            return new PropertiesConfigSource(filePath);
        }
        throw new ConfigValidationException("Unsupported ConfigMap file extension: " + filePath);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Map<String, String> load() {
        return delegate.load();
    }

}
