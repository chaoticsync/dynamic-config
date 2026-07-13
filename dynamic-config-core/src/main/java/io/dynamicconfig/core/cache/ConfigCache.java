package io.dynamicconfig.core.cache;

import java.util.Map;

public interface ConfigCache {

    String get(String key);

    boolean contains(String key);

    Map<String, String> snapshot();

    void replace(Map<String, String> newValues);

    void clear();

}