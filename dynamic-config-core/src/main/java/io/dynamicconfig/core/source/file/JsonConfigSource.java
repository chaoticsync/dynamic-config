package io.dynamicconfig.core.source.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dynamicconfig.core.exception.ConfigParseException;
import io.dynamicconfig.core.source.MapFlattener;

import java.util.Map;

public class JsonConfigSource extends AbstractFileConfigSource {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public JsonConfigSource(String file) {
        super(file);
    }

    @Override
    public Map<String, String> load() {
        String json = readFile();
        try {
            Map<String, Object> data = MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
            return MapFlattener.flatten(data);
        } catch (Exception e) {
            throw new ConfigParseException("Unable to parse JSON", e);
        }

    }

}