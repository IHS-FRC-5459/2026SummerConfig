package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Shared Jackson configuration for typed config files. */
final class TypedConfigMapper {

  private static final ObjectMapper MAPPER = createMapper();

  private TypedConfigMapper() {}

  static ObjectMapper mapper() {
    return MAPPER;
  }

  private static ObjectMapper createMapper() {
    ObjectMapper mapper =
        new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.DEFAULT);
    ConfigTypeRegistry.registerSubtypes(mapper);
    return mapper;
  }
}
