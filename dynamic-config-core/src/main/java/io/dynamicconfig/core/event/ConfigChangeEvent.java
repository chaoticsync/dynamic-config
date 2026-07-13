package io.dynamicconfig.core.event;

public record ConfigChangeEvent(String key, String oldValue, String newValue) {

}