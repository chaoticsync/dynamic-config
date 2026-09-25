package io.dynamicconfig.demo;

import io.dynamicconfig.core.client.ConfigClient;
import io.dynamicconfig.kubernetes.builder.KubernetesConfigExtensions;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simulates a Kubernetes ConfigMap mounted as a single file (no cluster required).
 *
 * <p>Run from the repo root:
 * {@code mvn -pl dynamic-config-demo -am exec:java -Dexec.mainClass=io.dynamicconfig.demo.KubernetesDemo}
 */
public final class KubernetesDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Dynamic Config SDK — Kubernetes File Demo ===\n");

        Path mountDir = Files.createTempDirectory("configmap-mount-");
        Path mountedFile = mountDir.resolve("application.yaml");
        Files.writeString(mountedFile, """
                server:
                  port: 10000
                feature:
                  enabled: true
                """);

        try {
            ConfigClient client = KubernetesConfigExtensions
                    .extend(ConfigClient.builder())
                    .configMapFile(mountedFile.toString())
                    .builder()
                    .properties(DemoConfigPaths.resolve("application.properties"))
                    .yaml(DemoConfigPaths.resolve("application.yaml"))
                    .build();

            client.start();

            System.out.println("Mounted ConfigMap file: " + mountedFile);
            System.out.println("Effective configuration (ConfigMap wins at HIGHEST priority):");
            System.out.println("  server.port       = " + client.getInt("server.port")
                    + "  (ConfigMap: 10000, yaml: 9000, properties: 8080)");
            System.out.println("  server.host       = " + client.get("server.host")
                    + "  (from properties — not in ConfigMap)");
            System.out.println("  feature.enabled   = " + client.getBoolean("feature.enabled"));
            System.out.println("  app.name          = " + client.get("app.name")
                    + "  (from yaml — not in ConfigMap)");

            System.out.println("\nSimulating ConfigMap volume update...");
            Files.writeString(mountedFile, """
                    server:
                      port: 11000
                    feature:
                      enabled: false
                    """);
            client.refresh();

            System.out.println("After refresh:");
            System.out.println("  server.port       = " + client.getInt("server.port"));
            System.out.println("  feature.enabled   = " + client.getBoolean("feature.enabled"));

            client.shutdown();
        } finally {
            deleteRecursively(mountDir);
        }

        System.out.println("\nDone.");
    }

    private static void deleteRecursively(Path root) throws Exception {
        if (!Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception ignored) {
                    // best-effort cleanup for a demo temp directory
                }
            });
        }
    }

}
