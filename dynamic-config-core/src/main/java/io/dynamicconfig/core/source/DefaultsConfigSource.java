package io.dynamicconfig.core.source;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class DefaultsConfigSource implements ConfigSource {

    private final Map<String, String> defaults;

    public DefaultsConfigSource(Map<String, String> defaults) {
        Objects.requireNonNull(defaults, "defaults cannot be null");
        this.defaults = Map.copyOf(defaults);
    }

    @Override
    public String getName() {
        return "defaults";
    }

    @Override
    public Map<String, String> load() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(defaults));
    }

}
