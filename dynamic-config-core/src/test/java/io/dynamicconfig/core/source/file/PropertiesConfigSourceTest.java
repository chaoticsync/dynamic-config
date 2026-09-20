package io.dynamicconfig.core.source.file;

import io.dynamicconfig.core.exception.ConfigValidationException;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesConfigSourceTest {

    @Test
    void shouldLoadProperties() {

        URL resource = getClass().getClassLoader().getResource("application.properties");

        assertNotNull(resource);

        PropertiesConfigSource source = new PropertiesConfigSource(Paths.get(resource.getPath()).toString());

        Map<String,String> map = source.load();

        assertEquals("8080", map.get("server.port"));
        assertEquals("localhost", map.get("server.host"));
        assertEquals("mysql", map.get("database.host"));
        assertEquals("3306", map.get("database.port"));
    }

    @Test
    void shouldThrowWhenFileDoesNotExist() {

        PropertiesConfigSource source = new PropertiesConfigSource("abc.properties");

        assertThrows(ConfigValidationException.class, source::load);
    }
}