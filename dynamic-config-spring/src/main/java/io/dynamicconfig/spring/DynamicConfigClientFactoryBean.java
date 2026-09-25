package io.dynamicconfig.spring;

import io.dynamicconfig.core.client.ConfigClient;
import io.dynamicconfig.core.client.ConfigClientBuilder;
import io.dynamicconfig.core.source.ConfigSource;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DynamicConfigClientFactoryBean
        implements FactoryBean<ConfigClient>, InitializingBean {

    private Map<String, String> defaults = new LinkedHashMap<>();
    private String[] yamlLocations;
    private String[] propertiesLocations;
    private String[] jsonLocations;
    private List<ConfigSource> sources = new ArrayList<>();
    private Long refreshIntervalSeconds;

    private ConfigClient configClient;

    public void setDefaults(Map<String, String> defaults) {
        this.defaults = defaults == null ? new LinkedHashMap<>() : new LinkedHashMap<>(defaults);
    }

    public void setYamlLocations(String... yamlLocations) {
        this.yamlLocations = yamlLocations;
    }

    public void setPropertiesLocations(String... propertiesLocations) {
        this.propertiesLocations = propertiesLocations;
    }

    public void setJsonLocations(String... jsonLocations) {
        this.jsonLocations = jsonLocations;
    }

    public void setSources(List<ConfigSource> sources) {
        this.sources = sources == null ? new ArrayList<>() : new ArrayList<>(sources);
    }

    public void setRefreshIntervalSeconds(Long refreshIntervalSeconds) {
        this.refreshIntervalSeconds = refreshIntervalSeconds;
    }

    @Override
    public void afterPropertiesSet() {
        ConfigClientBuilder builder = ConfigClient.builder();

        if (!defaults.isEmpty()) {
            builder.defaults(defaults);
        }

        if (propertiesLocations != null) {
            for (String location : propertiesLocations) {
                builder.properties(location);
            }
        }

        if (yamlLocations != null) {
            for (String location : yamlLocations) {
                builder.yaml(location);
            }
        }

        if (jsonLocations != null) {
            for (String location : jsonLocations) {
                builder.json(location);
            }
        }

        for (ConfigSource source : sources) {
            builder.addSource(source);
        }

        if (refreshIntervalSeconds != null) {
            builder.refreshInterval(Duration.ofSeconds(refreshIntervalSeconds));
        }

        configClient = builder.build();
        configClient.refresh();
    }

    @Override
    public ConfigClient getObject() {
        return configClient;
    }

    @Override
    public Class<?> getObjectType() {
        return ConfigClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
