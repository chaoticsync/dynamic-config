package io.dynamicconfig.spring.processor;

import io.dynamicconfig.core.client.ConfigClient;
import io.dynamicconfig.spring.annotation.DynamicValue;
import io.dynamicconfig.spring.conversion.DynamicValueConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

public class DynamicValueBeanPostProcessor implements BeanPostProcessor {

    private final ConfigClient configClient;

    public DynamicValueBeanPostProcessor(ConfigClient configClient) {
        this.configClient = configClient;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {

        Class<?> clazz = bean.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                processField(bean, field);
            }
            clazz = clazz.getSuperclass();
        }

        return bean;
    }

    private void processField(Object bean, Field field) {
        DynamicValue annotation = field.getAnnotation(DynamicValue.class);
        if (annotation == null) {
            return;
        }

        String key = annotation.value();
        injectValue(bean, field, key, configClient.get(key));
        registerWatcher(bean, field, key);
    }

    private void injectValue(Object bean, Field field, String key, String value) {
        if (value == null) {
            return;
        }

        Object convertedValue = DynamicValueConverter.convert(key, value, field.getType());
        field.setAccessible(true);

        try {
            field.set(bean, convertedValue);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(
                    "Failed to inject configuration for field: " + field.getName(), ex);
        }
    }

    private void registerWatcher(Object bean, Field field, String key) {
        field.setAccessible(true);
        configClient.watch(key, (changedKey, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            try {
                Object convertedValue = DynamicValueConverter.convert(key, newValue, field.getType());
                field.set(bean, convertedValue);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(
                        "Failed to update configuration for field: " + field.getName(), ex);
            }
        });
    }

}
