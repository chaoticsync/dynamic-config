package io.dynamicconfig.core.source;

/**
 * A configuration source that requires explicit startup and shutdown, such as
 * a remote watch or long-lived connection.
 */
public interface LifecycleConfigSource extends ConfigSource {

    void start(RefreshTrigger refreshTrigger);

    void stop();

}
