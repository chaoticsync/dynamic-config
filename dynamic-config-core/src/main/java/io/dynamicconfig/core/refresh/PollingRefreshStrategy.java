package io.dynamicconfig.core.refresh;

import io.dynamicconfig.core.source.RefreshTrigger;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PollingRefreshStrategy implements RefreshStrategy {

    private static final Logger LOGGER = Logger.getLogger(PollingRefreshStrategy.class.getName());

    private final RefreshTrigger refreshTrigger;
    private final Duration interval;
    private volatile ScheduledExecutorService executor;

    public PollingRefreshStrategy(RefreshTrigger refreshTrigger, Duration interval) {
        this.refreshTrigger = refreshTrigger;
        this.interval = interval;
    }

    @Override
    public void start() {
        if (executor != null) {
            return;
        }
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "dynamic-config-refresh");
            thread.setDaemon(true);
            return thread;
        });
        scheduledExecutor.scheduleAtFixedRate(
                () -> {
                    try {
                        refreshTrigger.refresh();
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Scheduled refresh failed", ex);
                    }
                },
                interval.toMillis(),
                interval.toMillis(),
                TimeUnit.MILLISECONDS);
        executor = scheduledExecutor;
    }

    @Override
    public void stop() {
        ScheduledExecutorService current = executor;
        if (current == null) {
            return;
        }
        executor = null;
        current.shutdownNow();
    }

}
