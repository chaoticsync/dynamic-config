package io.dynamicconfig.core.source.file;

import io.dynamicconfig.core.exception.ConfigLoadException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesConfigSource extends AbstractFileConfigSource {

    public PropertiesConfigSource(String file) {
        super(file);
    }

    @Override
    public Map<String, String> load() {

        Properties properties = new Properties();
        String content = readFile();
        try {
            properties.load(new StringReader(content));
        } catch (IOException ex) {
            throw new ConfigLoadException("Unable to load properties file", ex);
        }

        Map<String, String> map = new LinkedHashMap<>();

        for (String key : properties.stringPropertyNames()) {
            map.put(key, properties.getProperty(key));
        }

        return Collections.unmodifiableMap(map);
    }

}