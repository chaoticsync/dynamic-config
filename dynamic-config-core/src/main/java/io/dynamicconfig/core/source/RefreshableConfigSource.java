package io.dynamicconfig.core.source;

import java.util.Map;

public interface RefreshableConfigSource extends ConfigSource {

    /**
     * Reload configuration.
     */
    Map<String, String> reload() throws Exception;

}