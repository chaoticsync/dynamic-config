package io.dynamicconfig.demo;

import io.dynamicconfig.core.client.ConfigClient;
import io.dynamicconfig.core.source.MutableConfigSource;
import io.dynamicconfig.core.source.SourcePriority;

import java.util.Map;

/**
 * Standalone demo: file sources, precedence, typed getters, watchers, and refresh.
 *
 * <p>Run from the repo root:
 * {@code mvn -pl dynamic-config-demo -am exec:java}
 */
public final class CoreDemo {

    public static void main(String[] args) {
        System.out.println("=== Dynamic Config SDK — Core Demo ===\n");

        MutableConfigSource liveOverrides = new MutableConfigSource("live-overrides");

        ConfigClient client = ConfigClient.builder()
                .defaults(Map.of(
                        "server.port", "8000",
                        "feature.enabled", "false"))
                .properties(DemoConfigPaths.resolve("application.properties"))
                .yaml(DemoConfigPaths.resolve("application.yaml"))
                .addSource(liveOverrides, SourcePriority.HIGH)
                .build();

        client.watch("server.port", (key, oldValue, newValue) ->
                System.out.printf("  watcher: %s changed %s -> %s%n", key, oldValue, newValue));

        client.start();

        printEffectiveConfig(client);

        System.out.println("\nApplying live override (server.port=10000)...");
        liveOverrides.set("server.port", "10000");
        client.refresh();

        printEffectiveConfig(client);

        client.shutdown();
        System.out.println("\nDone.");
    }

    private static void printEffectiveConfig(ConfigClient client) {
        System.out.println("Effective configuration:");
        System.out.println("  server.port       = " + client.getInt("server.port")
                + "  (yaml beats properties and defaults)");
        System.out.println("  server.host       = " + client.get("server.host"));
        System.out.println("  database.host     = " + client.get("database.host"));
        System.out.println("  feature.enabled   = " + client.getBoolean("feature.enabled"));
        System.out.println("  app.name          = " + client.get("app.name"));
    }

}
