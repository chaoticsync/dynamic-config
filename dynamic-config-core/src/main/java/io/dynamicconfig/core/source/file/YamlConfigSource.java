package io.dynamicconfig.core.source.file;

import io.dynamicconfig.core.exception.ConfigParseException;
import io.dynamicconfig.core.exception.ConfigValidationException;
import io.dynamicconfig.core.source.MapFlattener;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class YamlConfigSource extends AbstractFileConfigSource {

    private static final Yaml YAML = new Yaml();

    public YamlConfigSource(String file) {
        super(file);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> load() {
        String yaml = readFile();
        try {
            Map<String, Object> data = YAML.load(yaml);
            return MapFlattener.flatten(data);
        } catch (Exception e) {
            throw new ConfigParseException("Unable to parse YAML", e);
        }

    }

}