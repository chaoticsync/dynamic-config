package io.dynamicconfig.spring;

import io.dynamicconfig.spring.annotation.DynamicValue;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XmlConfigurationTest {

    @Test
    void shouldWireBeansFromXmlConfiguration() {
        try (ClassPathXmlApplicationContext context =
                     new ClassPathXmlApplicationContext("spring/dynamic-config-context.xml")) {

            ServerConfig serverConfig = context.getBean("serverConfig", ServerConfig.class);
            assertEquals(8080, serverConfig.getPort());
        }
    }

    public static class ServerConfig {

        @DynamicValue("server.port")
        private Integer port;

        public Integer getPort() {
            return port;
        }
    }

}
