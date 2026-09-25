package io.dynamicconfig.demo.spring;

import io.dynamicconfig.core.client.ConfigClient;
import io.dynamicconfig.core.source.MutableConfigSource;
import io.dynamicconfig.core.source.SourcePriority;
import io.dynamicconfig.demo.DemoConfigPaths;
import io.dynamicconfig.spring.DynamicConfigSpringLifecycle;
import io.dynamicconfig.spring.processor.DynamicValueBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
class DynamicConfigInfrastructure {

    @Bean
    MutableConfigSource liveOverrides() {
        return new MutableConfigSource("live-overrides");
    }

    @Bean
    ConfigClient configClient(MutableConfigSource liveOverrides) {
        ConfigClient client = ConfigClient.builder()
                .defaults(Map.of(
                        "server.port", "8000",
                        "feature.enabled", "false"))
                .properties(DemoConfigPaths.resolve("application.properties"))
                .yaml(DemoConfigPaths.resolve("application.yaml"))
                .addSource(liveOverrides, SourcePriority.HIGH)
                .build();
        client.refresh();
        return client;
    }

    @Bean
    static DynamicValueBeanPostProcessor dynamicValueBeanPostProcessor(ConfigClient configClient) {
        return new DynamicValueBeanPostProcessor(configClient);
    }

    @Bean
    DynamicConfigSpringLifecycle dynamicConfigSpringLifecycle(ConfigClient configClient) {
        return new DynamicConfigSpringLifecycle(configClient);
    }

}
