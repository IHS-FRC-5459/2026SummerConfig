package org.team5459.config;

public class ConfigValue<T> {

    private final String key;
    private T value;

    public ConfigValue(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
