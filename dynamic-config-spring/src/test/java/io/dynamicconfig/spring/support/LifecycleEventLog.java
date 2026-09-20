package io.dynamicconfig.spring.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LifecycleEventLog {

    public enum Event {
        CONFIG_REFRESHED,
        DYNAMIC_VALUE_INJECTED,
        BACKGROUND_STARTED
    }

    private static final List<Event> EVENTS = new ArrayList<>();

    private LifecycleEventLog() {
    }

    public static void record(Event event) {
        EVENTS.add(event);
    }

    public static List<Event> events() {
        return Collections.unmodifiableList(EVENTS);
    }

    public static void clear() {
        EVENTS.clear();
    }

}
