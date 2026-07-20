package org.team5459.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Reflection helper methods used throughout the configuration library.
 *
 * <p>Centralizing reflection logic keeps the rest of the codebase simpler and provides a single
 * place to modify reflection behavior in the future.
 */
public final class ReflectionWalker {

  /** Utility class; should never be instantiated. */
  private ReflectionWalker() {}

  /**
   * Returns every public, non-static field of the configuration class.
   *
   * <p>Static fields are ignored because they are not configuration values.
   *
   * @param configType Configuration class
   * @return Public instance fields
   */
  public static Field[] getPublicInstanceFields(Class<?> configType) {
    return Arrays.stream(configType.getFields())
        .filter(field -> !Modifier.isStatic(field.getModifiers()))
        .toArray(Field[]::new);
  }

  /**
   * Reads the value of a reflected field.
   *
   * @param field Field to read
   * @param config Configuration object
   * @return Current field value
   */
  public static Object getValue(Field field, Object config) {
    try {
      return field.get(config);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Unable to read config field: " + field.getName(), e);
    }
  }

  /**
   * Writes a value into a reflected field.
   *
   * @param field Field to write
   * @param config Configuration object
   * @param value New value
   */
  public static void setValue(Field field, Object config, Object value) {
    try {
      field.set(config, value);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Unable to write config field: " + field.getName(), e);
    }
  }
}
