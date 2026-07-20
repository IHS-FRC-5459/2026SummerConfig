package org.team5459.config;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Copies values from a {@link JsonTree} into a configuration object.
 *
 * <p>Fields are matched by name using reflection. Every public instance field in the configuration
 * object must have a corresponding JSON value.
 */
public final class ReflectionMapper {

  /** Utility class; should never be instantiated. */
  private ReflectionMapper() {}

  /**
   * Populates a configuration object from a parsed JSON tree.
   *
   * @param jsonTree Parsed JSON
   * @param config Configuration object to populate
   */
  public static void populate(JsonTree jsonTree, Object config) {
    JsonNode root = jsonTree.getRoot();

    for (var field : ReflectionWalker.getPublicInstanceFields(config.getClass())) {

      // Look up the JSON property that matches the field name.
      JsonNode value = root.get(field.getName());

      if (value == null) {
        throw new IllegalArgumentException("Missing config value: " + field.getName());
      }

      if (field.getType() == double.class || field.getType() == Double.class) {
        ReflectionWalker.setValue(field, config, value.asDouble());

      } else if (field.getType() == int.class || field.getType() == Integer.class) {
        ReflectionWalker.setValue(field, config, value.asInt());

      } else {
        throw new IllegalArgumentException(
            "Unsupported config field type for " + field.getName() + ": " + field.getType());
      }
    }
  }
}
