package io.dynamicconfig.demo;

import io.dynamicconfig.core.client.ConfigClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Automatic polling refresh: file changes on disk are picked up by {@code refreshInterval}.
 *
 * <p>Run from the repo root:
 * {@code mvn -pl dynamic-config-demo -am exec:java -Dexec.mainClass=io.dynamicconfig.demo.PollingDemo}
 */
public final class PollingDemo {

    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    public static void main(String[] args) throws Exception {
        System.out.println("=== Dynamic Config SDK — Polling Demo ===\n");

        Path configDir = Files.createTempDirectory("polling-config-");
        Path configFile = configDir.resolve("application.yaml");
        Files.writeString(configFile, """
                server:
                  port: 8080
                """);

        CountDownLatch changeDetected = new CountDownLatch(1);
        AtomicReference<String> watchedOldValue = new AtomicReference<>();
        AtomicReference<String> watchedNewValue = new AtomicReference<>();

        try {
            ConfigClient client = ConfigClient.builder()
                    .yaml(configFile.toString())
                    .refreshInterval(POLL_INTERVAL)
                    .build();

            client.watch("server.port", (key, oldValue, newValue) -> {
                if ("9090".equals(newValue)) {
                    watchedOldValue.set(oldValue);
                    watchedNewValue.set(newValue);
                    changeDetected.countDown();
                }
            });

            client.start();

            System.out.println("Polling interval: " + POLL_INTERVAL.toMillis() + " ms");
            System.out.println("Config file: " + configFile);
            System.out.println("Initial server.port = " + client.getInt("server.port"));

            Thread.sleep(200);
            Files.writeString(configFile, """
                    server:
                      port: 9090
                    """);
            System.out.println("\nUpdated file on disk (8080 -> 9090), waiting for scheduled refresh...");

            long waitMillis = POLL_INTERVAL.multipliedBy(4).toMillis();
            boolean detected = changeDetected.await(waitMillis, TimeUnit.MILLISECONDS);

            System.out.println("After polling:");
            System.out.println("  server.port = " + client.getInt("server.port"));
            if (detected) {
                System.out.printf("  watcher fired: %s -> %s%n",
                        watchedOldValue.get(), watchedNewValue.get());
            } else {
                System.out.println("  watcher did not fire within " + waitMillis + " ms");
            }

            client.shutdown();
        } finally {
            deleteRecursively(configDir);
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
