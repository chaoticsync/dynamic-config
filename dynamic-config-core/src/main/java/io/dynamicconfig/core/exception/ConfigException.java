package io.dynamicconfig.core.exception;

/**
 * Thrown when a configuration file cannot be parsed.
 */
public class ConfigException extends RuntimeException {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}