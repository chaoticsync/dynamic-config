package io.dynamicconfig.demo.spring;

import io.dynamicconfig.core.client.ConfigClient;
import io.dynamicconfig.core.source.MutableConfigSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Spring {@code @DynamicValue} demo — field injection and runtime updates.
 *
 * <p>Run from the repo root:
 * {@code mvn -pl dynamic-config-demo -am exec:java -Dexec.mainClass=io.dynamicconfig.demo.spring.SpringDemo}
 */
public final class SpringDemo {

    private SpringDemo() {
    }

    public static void main(String[] args) {
        System.out.println("=== Dynamic Config SDK — Spring @DynamicValue Demo ===\n");
        System.out.println("See AppSettings.java for annotated fields.\n");

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(DemoSpringConfiguration.class)) {

            AppSettings settings = context.getBean(AppSettings.class);

            System.out.println("After Spring startup (fields injected from config):");
            printAnnotatedValues(settings);

            MutableConfigSource liveOverrides = context.getBean(MutableConfigSource.class);
            ConfigClient client = context.getBean(ConfigClient.class);

            System.out.println("\nApplying live overrides via MutableConfigSource + refresh()...");
            liveOverrides.set("server.port", "10000");
            liveOverrides.set("database.timeout", "30000");
            liveOverrides.set("feature.enabled", "false");
            liveOverrides.set("service.threshold", "0.9");
            liveOverrides.set("service.ratio", "0.25");
            client.refresh();

            System.out.println("\nAfter refresh (annotated fields updated in place):");
            printAnnotatedValues(settings);
        }

        System.out.println("\nDone.");
    }

    private static void printAnnotatedValues(AppSettings settings) {
        System.out.println("  @DynamicValue(\"server.port\")         int     -> " + settings.getServerPort());
        System.out.println("  @DynamicValue(\"server.host\")         String  -> " + settings.getServerHost());
        System.out.println("  @DynamicValue(\"database.timeout\")    long    -> " + settings.getDatabaseTimeoutMs());
        System.out.println("  @DynamicValue(\"feature.enabled\")     boolean -> " + settings.isFeatureEnabled());
        System.out.println("  @DynamicValue(\"service.threshold\")   double  -> " + settings.getServiceThreshold());
        System.out.println("  @DynamicValue(\"service.ratio\")       float   -> " + settings.getServiceRatio());
        System.out.println("  @DynamicValue(\"app.name\")            String  -> " + settings.getAppName());
    }

}
