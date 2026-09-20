package io.dynamicconfig.spring;

import io.dynamicconfig.core.client.ConfigClient;
import org.springframework.context.SmartLifecycle;

public class DynamicConfigSpringLifecycle implements SmartLifecycle {

    public static final int PHASE = SmartLifecycle.DEFAULT_PHASE + 1000;

    private final ConfigClient configClient;
    private volatile boolean running;

    public DynamicConfigSpringLifecycle(ConfigClient configClient) {
        this.configClient = configClient;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        configClient.startBackgroundLifecycle();
        running = true;
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        configClient.shutdown();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return PHASE;
    }

}
