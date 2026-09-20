package io.dynamicconfig.kubernetes.builder;

import io.dynamicconfig.core.client.ConfigClientBuilder;
import io.dynamicconfig.core.source.MutableConfigSource;
import io.dynamicconfig.core.source.SourcePriority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KubernetesConfigExtensionsTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldRegisterConfigMapFileWithHighestPriority() throws Exception {
        Path file = tempDir.resolve("application.yaml");
        Files.writeString(file, """
                server:
                  port: 10000
                """);

        MutableConfigSource lowerPriority = new MutableConfigSource("lower", Map.of("server.port", "8080"));

        var client = KubernetesConfigExtensions.extend(ConfigClientBuilder.builder())
                .configMapFile(file.toString())
                .builder()
                .addSource(lowerPriority, SourcePriority.LOW)
                .build();

        client.start();

        assertEquals("10000", client.get("server.port"));
        client.shutdown();
    }

}
