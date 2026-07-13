package io.dynamicconfig.core.source;

import java.util.Objects;

public final class SourceRegistration {

    private final ConfigSource source;

    private final int priority;

    public SourceRegistration(ConfigSource source, int priority) {

        this.source = Objects.requireNonNull(source);

        this.priority = priority;

    }

    public ConfigSource getSource() {

        return source;

    }

    public int getPriority() {

        return priority;

    }

}