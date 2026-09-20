package io.dynamicconfig.core.source;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MapFlattener {

    private MapFlattener() {
    }

    public static Map<String, String> flatten(Map<String, Object> source) {

        Map<String, String> result = new LinkedHashMap<>();

        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }

        flattenMap("", source, result);

        return Collections.unmodifiableMap(result);
    }

    @SuppressWarnings("unchecked")
    private static void flattenMap(
            String prefix,
            Map<String, Object> source,
            Map<String, String> result) {

        for (Map.Entry<String, Object> entry : source.entrySet()) {

            String key = prefix.isEmpty()
                    ? entry.getKey()
                    : prefix + "." + entry.getKey();

            Object value = entry.getValue();

            if (value == null) {
                result.put(key, null);
            }
            else if (value instanceof Map) {
                flattenMap(
                        key,
                        (Map<String, Object>) value,
                        result
                );
            }
            else if (value instanceof List) {
                flattenList(
                        key,
                        (List<?>) value,
                        result
                );
            }
            else {
                result.put(key, String.valueOf(value));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void flattenList(
            String prefix,
            List<?> list,
            Map<String, String> result) {

        for (int i = 0; i < list.size(); i++) {

            Object value = list.get(i);

            String key = prefix + "[" + i + "]";

            if (value == null) {
                result.put(key, null);
            }
            else if (value instanceof Map) {
                flattenMap(
                        key,
                        (Map<String, Object>) value,
                        result
                );
            }
            else if (value instanceof List) {
                flattenList(
                        key,
                        (List<?>) value,
                        result
                );
            }
            else {
                result.put(key, String.valueOf(value));
            }
        }
    }

}