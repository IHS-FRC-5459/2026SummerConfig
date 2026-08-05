package org.team5459.config;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Deep-copies a {@link ConfigNode} tree via JSON round-trip so inserts never share instances. */
final class ConfigNodeCloner {

  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  private ConfigNodeCloner() {}

  static ConfigNode deepCopy(ConfigNode node) {
    if (node == null) {
      return null;
    }
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (JsonGenerator generator = JSON_FACTORY.createGenerator(bytes, JsonEncoding.UTF8)) {
        generator.writeStartObject();
        generator.writeFieldName("copy");
        ConfigJsonWriter.writeNode(node, generator);
        generator.writeEndObject();
      }
      Map<String, ConfigNode> parsed =
          TypedConfigMapper.mapper()
              .readValue(
                  bytes.toByteArray(), new TypeReference<LinkedHashMap<String, ConfigNode>>() {});
      ConfigNode copy = parsed.get("copy");
      if (copy != null) {
        ConfigNode.initializeTree(copy);
      }
      return copy;
    } catch (IOException exception) {
      throw new UncheckedIOException("Unable to clone config node", exception);
    }
  }
}
