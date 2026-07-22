package org.team5459.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Shared Jackson {@link ObjectMapper} configured for typed config files.
 *
 * <p>Visibility is restricted to creators so only {@code @JsonCreator} constructors and
 * {@code @JsonProperty} parameters participate in (de)serialization. Unknown JSON properties are
 * ignored to tolerate hand-edited files. Subtype registration is delegated to {@link
 * ConfigTypeRegistry}.
 */
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
