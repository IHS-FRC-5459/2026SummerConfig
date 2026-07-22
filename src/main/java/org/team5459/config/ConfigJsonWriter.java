package org.team5459.config;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.team5459.config.types.*;

/**
 * Writes typed config files using explicit JSON generation.
 *
 * <p>Jackson deserialization is used for loading, but saving goes through this writer so only the
 * schema fields {@code type} and {@code value} are persisted. That avoids leaking runtime-only
 * bean properties such as {@code controller}, {@code rotation}, or {@code childEntries} that
 * appear on node getters.
 *
 * <p>The output mirrors the on-disk format expected by {@link TypedConfigLoader}: folders and
 * composites become nested objects, while scalar and array nodes write their payload directly as
 * the {@code value}.
 */
final class ConfigJsonWriter {

  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  private ConfigJsonWriter() {}

  static void write(File jsonFile, Map<String, ConfigNode> rootEntries) throws IOException {
    try (JsonGenerator generator =
        JSON_FACTORY.createGenerator(jsonFile, JsonEncoding.UTF8).useDefaultPrettyPrinter()) {
      generator.writeStartObject();
      for (Map.Entry<String, ConfigNode> entry : rootEntries.entrySet()) {
        generator.writeFieldName(entry.getKey());
        writeNode(entry.getValue(), generator);
      }
      generator.writeEndObject();
    }
  }

  private static void writeNode(ConfigNode node, JsonGenerator generator) throws IOException {
    generator.writeStartObject();
    generator.writeStringField("type", ConfigTypeRegistry.typeNameFor(node.getClass()));
    generator.writeFieldName("value");
    writeValue(node, generator);
    generator.writeEndObject();
  }

  private static void writeValue(ConfigNode node, JsonGenerator generator) throws IOException {
    if (node instanceof FolderNode folder) {
      writeNodeMap(folder.getChildren(), generator);
    } else if (node instanceof CompositeConfigNode composite) {
      writeNodeMap(composite.getFields(), generator);
    } else if (node instanceof DoubleNode doubleNode) {
      generator.writeNumber(doubleNode.getValue());
    } else if (node instanceof IntNode intNode) {
      generator.writeNumber(intNode.getValue());
    } else if (node instanceof BooleanNode booleanNode) {
      generator.writeBoolean(booleanNode.getValue());
    } else if (node instanceof StringNode stringNode) {
      generator.writeString(stringNode.getValue());
    } else if (node instanceof DoubleArrayNode arrayNode) {
      generator.writeArray(arrayNode.getValue(), 0, arrayNode.getValue().length);
    } else if (node instanceof IntArrayNode arrayNode) {
      int[] values = arrayNode.getValue();
      generator.writeStartArray();
      for (int value : values) {
        generator.writeNumber(value);
      }
      generator.writeEndArray();
    } else {
      throw new IllegalArgumentException(
          "Unable to serialize config node type: " + node.getClass().getName());
    }
  }

  private static void writeNodeMap(Map<String, ConfigNode> entries, JsonGenerator generator)
      throws IOException {
    generator.writeStartObject();
    for (Map.Entry<String, ConfigNode> entry : entries.entrySet()) {
      generator.writeFieldName(entry.getKey());
      writeNode(entry.getValue(), generator);
    }
    generator.writeEndObject();
  }
}
