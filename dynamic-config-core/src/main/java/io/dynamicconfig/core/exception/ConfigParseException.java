package io.dynamicconfig.core.exception;

/**
 * Thrown when a configuration file cannot be parsed.
 */
public class ConfigParseException extends ConfigException {

    public ConfigParseException(String message, Throwable cause) {
        super(message, cause);
    }

}