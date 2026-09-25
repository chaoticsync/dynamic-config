package io.dynamicconfig.demo;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public final class DemoConfigPaths {

    private DemoConfigPaths() {
    }

    public static String resolve(String resourceName) {
        URL resource = DemoConfigPaths.class.getClassLoader().getResource(resourceName);
        if (resource == null) {
            throw new IllegalStateException("Missing classpath resource: " + resourceName);
        }
        try {
            return Path.of(resource.toURI()).toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to resolve resource: " + resourceName, e);
        }
    }

}
