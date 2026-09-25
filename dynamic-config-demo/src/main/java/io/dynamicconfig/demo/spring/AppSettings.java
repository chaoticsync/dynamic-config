package io.dynamicconfig.demo.spring;

import io.dynamicconfig.spring.annotation.DynamicValue;
import org.springframework.stereotype.Component;

/**
 * Bean whose fields are injected and kept up to date by {@link DynamicValue}.
 *
 * <pre>{@code
 * @Component
 * public class AppSettings {
 *
 *     @DynamicValue("server.port")
 *     private int serverPort;
 * }
 * }</pre>
 */
@Component
public class AppSettings {

    @DynamicValue("server.port")
    private int serverPort;

    @DynamicValue("server.host")
    private String serverHost;

    @DynamicValue("database.timeout")
    private long databaseTimeoutMs;

    @DynamicValue("feature.enabled")
    private boolean featureEnabled;

    @DynamicValue("service.threshold")
    private double serviceThreshold;

    @DynamicValue("service.ratio")
    private float serviceRatio;

    @DynamicValue("app.name")
    private String appName;

    public int getServerPort() {
        return serverPort;
    }

    public String getServerHost() {
        return serverHost;
    }

    public long getDatabaseTimeoutMs() {
        return databaseTimeoutMs;
    }

    public boolean isFeatureEnabled() {
        return featureEnabled;
    }

    public double getServiceThreshold() {
        return serviceThreshold;
    }

    public float getServiceRatio() {
        return serviceRatio;
    }

    public String getAppName() {
        return appName;
    }

}
