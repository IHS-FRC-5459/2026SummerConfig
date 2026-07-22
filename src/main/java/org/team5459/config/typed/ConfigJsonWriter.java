package org.team5459.config.typed;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/** Writes typed config files without Jackson bean serialization. */
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
