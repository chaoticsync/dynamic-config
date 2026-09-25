package io.dynamicconfig.spring;

import io.dynamicconfig.core.client.ConfigClient;
import io.dynamicconfig.core.source.MutableConfigSource;
import io.dynamicconfig.spring.annotation.DynamicValue;
import io.dynamicconfig.spring.processor.DynamicValueBeanPostProcessor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicRefreshTest {

    @Test
    void shouldUpdateFieldWhenConfigurationChanges() {
        MutableConfigSource source = new MutableConfigSource(
                "test",
                Map.of("server.port", "8080"));

        ConfigClient client = ConfigClient.builder()
                .addSource(source)
                .build();
        client.refresh();

        DynamicValueBeanPostProcessor processor = new DynamicValueBeanPostProcessor(client);
        ServerConfig serverConfig = new ServerConfig();
        processor.postProcessBeforeInitialization(serverConfig, "serverConfig");

        assertEquals(8080, serverConfig.getPort());

        source.set("server.port", "9090");
        client.refresh();

        assertEquals(9090, serverConfig.getPort());
    }

    static class ServerConfig {

        @DynamicValue("server.port")
        private Integer port;

        Integer getPort() {
            return port;
        }
    }

}
