package io.dynamicconfig.core.refresh;

import io.dynamicconfig.core.source.RefreshTrigger;

public final class RefreshTask implements Runnable {

    private final RefreshTrigger refreshTrigger;

    public RefreshTask(RefreshTrigger refreshTrigger) {
        this.refreshTrigger = refreshTrigger;
    }

    @Override
    public void run() {
        refreshTrigger.refresh();
    }

}
