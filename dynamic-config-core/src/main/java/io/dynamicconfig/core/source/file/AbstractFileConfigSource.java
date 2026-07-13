package io.dynamicconfig.core.source.file;

import io.dynamicconfig.core.exception.ConfigLoadException;
import io.dynamicconfig.core.exception.ConfigValidationException;
import io.dynamicconfig.core.source.ConfigSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractFileConfigSource implements ConfigSource {

    private final Path path;

    protected AbstractFileConfigSource(String filePath) {
        this.path = Path.of(filePath);
    }

    protected Path getPath() {
        return path;
    }

    protected String readFile() {

        validate();

        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new ConfigLoadException(
                    "Unable to read configuration file: " + path,
                    ex
            );
        }
    }

    protected void validate() {

        if (!Files.exists(path)) {
            throw new ConfigValidationException(
                    "Configuration file does not exist: " + path
            );
        }

        if (!Files.isRegularFile(path)) {
            throw new ConfigValidationException(
                    "Not a regular configuration file: " + path
            );
        }

        if (!Files.isReadable(path)) {
            throw new ConfigValidationException(
                    "Configuration file is not readable: " + path
            );
        }

    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

}