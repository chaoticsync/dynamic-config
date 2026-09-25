package io.dynamicconfig.spring.conversion;

import io.dynamicconfig.core.conversion.ConfigValueConverter;

public final class DynamicValueConverter {

    private DynamicValueConverter() {
    }

    public static Object convert(String key, String value, Class<?> targetType) {
        return ConfigValueConverter.convert(key, value, targetType);
    }

}
