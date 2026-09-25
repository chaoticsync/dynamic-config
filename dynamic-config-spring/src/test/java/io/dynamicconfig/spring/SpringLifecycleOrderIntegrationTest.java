package io.dynamicconfig.spring;

import io.dynamicconfig.spring.annotation.DynamicValue;
import io.dynamicconfig.spring.processor.DynamicValueBeanPostProcessor;
import io.dynamicconfig.spring.support.LifecycleEventLog;
import io.dynamicconfig.spring.support.LifecycleEventLog.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringLifecycleOrderIntegrationTest {

    @AfterEach
    void tearDown() {
        LifecycleEventLog.clear();
    }

    @Test
    void shouldInitializeInCorrectOrder() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(TestConfiguration.class)) {

            ProbeBean probeBean = context.getBean(ProbeBean.class);
            assertNotNull(probeBean.getFlag());
            assertTrue(probeBean.getFlag());

            List<Event> events = LifecycleEventLog.events();
            assertEquals(
                    List.of(
                            Event.CONFIG_REFRESHED,
                            Event.DYNAMIC_VALUE_INJECTED,
                            Event.BACKGROUND_STARTED),
                    events);
        }
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        RecordingFactoryBean configClient() {
            RecordingFactoryBean factoryBean = new RecordingFactoryBean();
            factoryBean.setDefaults(Map.of("app.flag", "true"));
            return factoryBean;
        }

        @Bean
        DynamicValueBeanPostProcessor dynamicValueBeanPostProcessor(
                io.dynamicconfig.core.client.ConfigClient configClient) {
            return new DynamicValueBeanPostProcessor(configClient);
        }

        @Bean
        RecordingLifecycle dynamicConfigSpringLifecycle(
                io.dynamicconfig.core.client.ConfigClient configClient) {
            return new RecordingLifecycle(configClient);
        }

        @Bean
        ProbeBean probeBean() {
            return new ProbeBean();
        }
    }

    static class RecordingFactoryBean extends DynamicConfigClientFactoryBean {

        @Override
        public void afterPropertiesSet() {
            super.afterPropertiesSet();
            LifecycleEventLog.record(Event.CONFIG_REFRESHED);
        }
    }

    static class RecordingLifecycle extends DynamicConfigSpringLifecycle {

        RecordingLifecycle(io.dynamicconfig.core.client.ConfigClient configClient) {
            super(configClient);
        }

        @Override
        public void start() {
            super.start();
            LifecycleEventLog.record(Event.BACKGROUND_STARTED);
        }
    }

    static class ProbeBean implements InitializingBean {

        @DynamicValue("app.flag")
        private Boolean flag;

        @Override
        public void afterPropertiesSet() {
            assertNotNull(flag);
            assertTrue(flag);
            LifecycleEventLog.record(Event.DYNAMIC_VALUE_INJECTED);
        }

        Boolean getFlag() {
            return flag;
        }
    }

}
