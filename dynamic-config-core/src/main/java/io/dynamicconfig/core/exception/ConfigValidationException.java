package io.dynamicconfig.core.exception;

/**
 * Thrown when a configuration file cannot be parsed.
 */
public class ConfigValidationException extends ConfigException {

    public ConfigValidationException(String message) {
        super(message);
    }

}