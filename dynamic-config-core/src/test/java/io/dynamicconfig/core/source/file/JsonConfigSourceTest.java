package io.dynamicconfig.core.source.file;

import io.dynamicconfig.core.exception.ConfigParseException;
import io.dynamicconfig.core.exception.ConfigValidationException;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonConfigSourceTest {

    @Test
    void shouldLoadJson() {

        URL resource = getClass().getClassLoader().getResource("application.json");

        JsonConfigSource source = new JsonConfigSource(Paths.get(resource.getPath()).toString());

        Map<String,String> map = source.load();

        assertEquals("8080", map.get("server.port"));
        assertEquals("mysql", map.get("database.host"));
        assertEquals("app1", map.get("servers[0]"));
        assertEquals("app2", map.get("servers[1]"));
    }

    @Test
    void shouldThrowOnInvalidJson() {

        URL resource = getClass().getClassLoader().getResource("invalid.json");
        JsonConfigSource source = new JsonConfigSource(Paths.get(resource.getPath()).toString());

        assertThrows(ConfigParseException.class, source::load);
    }

    @Test
    void shouldThrowWhenFileDoesNotExist() {

        JsonConfigSource source = new JsonConfigSource("abc.json");

        assertThrows(ConfigValidationException.class, source::load);
    }
}