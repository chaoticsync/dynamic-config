package io.dynamicconfig.core.source;

/**
 * Minimal callback used by lifecycle sources and scheduled refresh to trigger
 * a configuration reload without coupling to the full {@code ConfigClient}.
 */
@FunctionalInterface
public interface RefreshTrigger {

    void refresh();

}
