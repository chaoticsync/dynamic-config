package io.dynamicconfig.kubernetes.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dynamicconfig.core.exception.ConfigParseException;
import io.dynamicconfig.core.source.MapFlattener;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public final class ConfigMapDataParser {

    private static final Yaml YAML = new Yaml();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ConfigMapDataParser() {
    }

    public static Map<String, String> parseData(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return Map.of();
        }

        Map<String, String> merged = new LinkedHashMap<>();
        new TreeMap<>(data).forEach((key, content) -> merged.putAll(parseEntry(key, content)));
        return merged;
    }

    static Map<String, String> parseEntry(String key, String content) {
        String lowerKey = key.toLowerCase();
        if (lowerKey.endsWith(".yaml") || lowerKey.endsWith(".yml")) {
            return parseYaml(content);
        }
        if (lowerKey.endsWith(".json")) {
            return parseJson(content);
        }
        if (lowerKey.endsWith(".properties")) {
            return parseProperties(content);
        }
        return parseProperties(content);
    }

    private static Map<String, String> parseYaml(String content) {
        try {
            Map<String, Object> parsed = YAML.load(content);
            return MapFlattener.flatten(parsed);
        } catch (Exception ex) {
            throw new ConfigParseException("Unable to parse YAML from ConfigMap entry", ex);
        }
    }

    private static Map<String, String> parseJson(String content) {
        try {
            Map<String, Object> parsed = MAPPER.readValue(content, new TypeReference<Map<String, Object>>() {});
            return MapFlattener.flatten(parsed);
        } catch (Exception ex) {
            throw new ConfigParseException("Unable to parse JSON from ConfigMap entry", ex);
        }
    }

    private static Map<String, String> parseProperties(String content) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(content));
        } catch (IOException ex) {
            throw new ConfigParseException("Unable to parse properties from ConfigMap entry", ex);
        }

        Map<String, String> map = new LinkedHashMap<>();
        for (String propertyName : properties.stringPropertyNames()) {
            map.put(propertyName, properties.getProperty(propertyName));
        }
        return Collections.unmodifiableMap(map);
    }

}
