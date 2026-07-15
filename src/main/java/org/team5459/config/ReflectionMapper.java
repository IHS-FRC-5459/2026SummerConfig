package org.team5459.config;

import com.fasterxml.jackson.databind.JsonNode;

public final class ReflectionMapper {

  private ReflectionMapper() {}

  public static void populate(JsonTree jsonTree, Object config) {
    JsonNode root = jsonTree.getRoot();

    for (var field : ReflectionWalker.getPublicInstanceFields(config.getClass())) {
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
