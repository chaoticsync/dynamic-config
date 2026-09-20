package io.dynamicconfig.kubernetes.builder;

import io.dynamicconfig.core.client.ConfigClient;
import io.dynamicconfig.core.client.ConfigClientBuilder;
import io.dynamicconfig.core.source.SourcePriority;
import io.dynamicconfig.kubernetes.source.ConfigMapFileSource;
import io.dynamicconfig.kubernetes.watch.ConfigMapWatchSource;

import java.util.Objects;

/**
 * Kubernetes-specific extensions for {@link ConfigClientBuilder}.
 */
public final class KubernetesConfigExtensions {

    private final ConfigClientBuilder builder;

    private KubernetesConfigExtensions(ConfigClientBuilder builder) {
        this.builder = Objects.requireNonNull(builder, "builder cannot be null");
    }

    public static KubernetesConfigExtensions extend(ConfigClientBuilder builder) {
        return new KubernetesConfigExtensions(builder);
    }

    public KubernetesConfigExtensions configMapFile(String filePath) {
        builder.addSource(new ConfigMapFileSource(filePath), SourcePriority.HIGHEST);
        return this;
    }

    public KubernetesConfigExtensions configMapWatch(String namespace, String name) {
        builder.addSource(new ConfigMapWatchSource(namespace, name), SourcePriority.HIGHEST);
        return this;
    }

    public ConfigClientBuilder builder() {
        return builder;
    }

    public ConfigClient build() {
        return builder.build();
    }

}
