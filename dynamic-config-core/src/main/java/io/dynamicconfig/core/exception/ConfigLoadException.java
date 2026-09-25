package io.dynamicconfig.core.exception;

/**
 * Thrown when a configuration file cannot be parsed.
 */
public class ConfigLoadException extends ConfigException {

    public ConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }

}