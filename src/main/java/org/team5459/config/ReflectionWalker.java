package org.team5459.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public final class ReflectionWalker {

  private ReflectionWalker() {}

  public static Field[] getPublicInstanceFields(Class<?> configType) {
    return Arrays.stream(configType.getFields())
        .filter(field -> !Modifier.isStatic(field.getModifiers()))
        .toArray(Field[]::new);
  }

  public static Object getValue(Field field, Object config) {
    try {
      return field.get(config);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Unable to read config field: " + field.getName(), e);
    }
  }

  public static void setValue(Field field, Object config, Object value) {
    try {
      field.set(config, value);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Unable to write config field: " + field.getName(), e);
    }
  }
}
