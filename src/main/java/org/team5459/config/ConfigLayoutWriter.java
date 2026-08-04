package org.team5459.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import org.team5459.config.entries.BooleanEntry;
import org.team5459.config.entries.ConfigEntry;
import org.team5459.config.entries.DoubleEntry;
import org.team5459.config.entries.PIDControllerEntry;
import org.team5459.config.layout.ElasticContainer;
import org.team5459.config.layout.ElasticLayout;
import org.team5459.config.layout.ElasticProperties;

/**
 * Writes an Elastic-shaped {@code robot-config.json}: same layout tree as Save As, with {@code
 * properties.value} filled from the live {@link ConfigDocument}.
 */
public final class ConfigLayoutWriter {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  private ConfigLayoutWriter() {}

  /**
   * Copies current document values into the layout's widget {@code properties.value} fields, then
   * writes the layout to {@code configFile}.
   */
  public static void write(File configFile, ElasticLayout layout, ConfigDocument document) {
    if (configFile == null || layout == null || document == null) {
      return;
    }
    syncValues(layout, document);
    try {
      File parent = configFile.getParentFile();
      if (parent != null) {
        parent.mkdirs();
      }
      MAPPER.writeValue(configFile, layout);
    } catch (IOException exception) {
      ConfigWarnings.warn(
          "Failed to write robot config " + configFile + ": " + exception.getMessage());
    }
  }

  static void syncValues(ElasticLayout layout, ConfigDocument document) {
    for (ElasticContainer widget : ConfigLoader.allWidgets(layout)) {
      if (widget == null || widget.properties == null) {
        continue;
      }
      ElasticProperties properties = widget.properties;
      if (properties.topic == null || properties.topic.isBlank()) {
        continue;
      }
      ConfigEntry entry = document.getEntry(properties.topic);
      if (entry == null) {
        continue;
      }
      properties.value = toValueNode(entry);
    }
  }

  private static JsonNode toValueNode(ConfigEntry entry) {
    if (entry instanceof DoubleEntry doubleEntry) {
      return MAPPER.getNodeFactory().numberNode(doubleEntry.getValue());
    }
    if (entry instanceof BooleanEntry booleanEntry) {
      return MAPPER.getNodeFactory().booleanNode(booleanEntry.getValue());
    }
    if (entry instanceof PIDControllerEntry pidEntry) {
      ObjectNode object = MAPPER.createObjectNode();
      object.put("p", pidEntry.getP());
      object.put("i", pidEntry.getI());
      object.put("d", pidEntry.getD());
      object.put("setpoint", pidEntry.getSetpoint());
      return object;
    }
    return MAPPER.getNodeFactory().nullNode();
  }
}
