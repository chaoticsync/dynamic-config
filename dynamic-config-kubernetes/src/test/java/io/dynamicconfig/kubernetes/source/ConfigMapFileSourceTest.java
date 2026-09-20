package io.dynamicconfig.kubernetes.source;

import io.dynamicconfig.core.exception.ConfigValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigMapFileSourceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldLoadYamlConfigMapFile() throws IOException {
        Path file = tempDir.resolve("application.yaml");
        Files.writeString(file, """
                server:
                  port: 8080
                """);

        ConfigMapFileSource source = new ConfigMapFileSource(file.toString());
        Map<String, String> config = source.load();

        assertEquals("8080", config.get("server.port"));
        assertEquals("application.yaml", source.getName());
    }

    @Test
    void shouldLoadJsonConfigMapFile() throws IOException {
        Path file = tempDir.resolve("application.json");
        Files.writeString(file, """
                {"server":{"port":9090}}
                """);

        ConfigMapFileSource source = new ConfigMapFileSource(file.toString());
        Map<String, String> config = source.load();

        assertEquals("9090", config.get("server.port"));
    }

    @Test
    void shouldLoadPropertiesConfigMapFile() throws IOException {
        Path file = tempDir.resolve("application.properties");
        Files.writeString(file, "server.port=7070\n");

        ConfigMapFileSource source = new ConfigMapFileSource(file.toString());
        Map<String, String> config = source.load();

        assertEquals("7070", config.get("server.port"));
    }

    @Test
    void shouldRejectUnsupportedExtension() {
        assertThrows(
                ConfigValidationException.class,
                () -> new ConfigMapFileSource("/etc/config/application.txt")
        );
    }

}
