package io.dynamicconfig.core.source;

import java.util.Objects;

public final class SourceRegistration {

    private final ConfigSource source;
    private final SourcePriority priority;
    private final int registrationOrder;

    public SourceRegistration(ConfigSource source, SourcePriority priority, int registrationOrder) {
        this.source = Objects.requireNonNull(source, "source cannot be null");
        this.priority = Objects.requireNonNull(priority, "priority cannot be null");
        this.registrationOrder = registrationOrder;
    }

    public ConfigSource getSource() {
        return source;
    }

    public SourcePriority getPriority() {
        return priority;
    }

    int getRegistrationOrder() {
        return registrationOrder;
    }

}
