package io.dynamicconfig.core.conversion;

import io.dynamicconfig.core.exception.ConfigConversionException;

public final class ConfigValueConverter {

    private ConfigValueConverter() {
    }

    public static Integer toInteger(String key, String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new ConfigConversionException(
                    "Unable to convert configuration key '" + key + "' to Integer", ex);
        }
    }

    public static Long toLong(String key, String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new ConfigConversionException(
                    "Unable to convert configuration key '" + key + "' to Long", ex);
        }
    }

    public static Double toDouble(String key, String value) {
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            throw new ConfigConversionException(
                    "Unable to convert configuration key '" + key + "' to Double", ex);
        }
    }

    public static Boolean toBoolean(String key, String value) {
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    public static Object convert(String key, String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        }
        if (targetType == Integer.class || targetType == int.class) {
            return toInteger(key, value);
        }
        if (targetType == Long.class || targetType == long.class) {
            return toLong(key, value);
        }
        if (targetType == Double.class || targetType == double.class) {
            return toDouble(key, value);
        }
        if (targetType == Float.class || targetType == float.class) {
            Double converted = toDouble(key, value);
            return converted == null ? null : converted.floatValue();
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return toBoolean(key, value);
        }
        throw new ConfigConversionException(
                "Unsupported conversion type for key '" + key + "': " + targetType.getName(), null);
    }

}
