package org.team5459.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.team5459.config.entries.BooleanEntry;
import org.team5459.config.entries.ConfigEntry;
import org.team5459.config.entries.DoubleEntry;
import org.team5459.config.entries.PIDControllerEntry;
import org.team5459.config.layout.ElasticContainer;
import org.team5459.config.layout.ElasticLayout;
import org.team5459.config.layout.ElasticProperties;
import org.team5459.config.layout.ElasticTab;

/**
 * Loads an Elastic layout JSON file into a {@link ConfigDocument} (and keeps the layout tree for
 * writing {@code robot-config.json} back out).
 */
public final class ConfigLoader {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private ConfigLoader() {}

  /** Layout tree plus runtime entries built from it. */
  public record LoadedConfig(ElasticLayout layout, ConfigDocument document) {}

  public static ConfigDocument load(File file) {
    return loadWithLayout(file).document();
  }

  public static LoadedConfig loadWithLayout(File file) {
    try {
      ElasticLayout layout = MAPPER.readValue(file, ElasticLayout.class);
      return new LoadedConfig(layout, fromLayout(layout));
    } catch (IOException exception) {
      ConfigWarnings.warn("Failed to load config from " + file + ": " + exception.getMessage());
      return new LoadedConfig(new ElasticLayout(), new ConfigDocument(Map.of()));
    }
  }

  static ConfigDocument fromLayout(ElasticLayout layout) {
    Map<String, ConfigEntry> entries = new LinkedHashMap<>();
    for (ElasticContainer container : allWidgets(layout)) {
      ConfigEntry entry = toEntry(container);
      if (entry == null) {
        continue;
      }
      String topic = entry.topic();
      if (entries.containsKey(topic)) {
        ConfigWarnings.warn("Duplicate topic '" + topic + "'. Keeping the first container.");
        continue;
      }
      entries.put(topic, entry);
    }
    return new ConfigDocument(entries);
  }

  /** Depth-first list of every widget in tabs, including nested list-layout children. */
  static List<ElasticContainer> allWidgets(ElasticLayout layout) {
    List<ElasticContainer> widgets = new ArrayList<>();
    if (layout == null || layout.tabs == null) {
      return widgets;
    }
    for (ElasticTab tab : layout.tabs) {
      if (tab == null || tab.grid_layout == null) {
        continue;
      }
      collectWidgets(tab.grid_layout.layouts, widgets);
      collectWidgets(tab.grid_layout.containers, widgets);
    }
    return widgets;
  }

  private static void collectWidgets(List<ElasticContainer> nodes, List<ElasticContainer> out) {
    if (nodes == null) {
      return;
    }
    for (ElasticContainer node : nodes) {
      if (node == null) {
        continue;
      }
      out.add(node);
      if (node.children != null && !node.children.isEmpty()) {
        collectWidgets(node.children, out);
      }
    }
  }

  private static ConfigEntry toEntry(ElasticContainer container) {
    String title = container.title == null ? "(untitled)" : container.title;
    String type = container.type;
    if (type == null || type.isBlank()) {
      ConfigWarnings.warnUnsupportedWidget(title, String.valueOf(type));
      return null;
    }

    // Layout chrome only — children are collected separately.
    if ("List Layout".equals(type)) {
      return null;
    }

    ElasticProperties properties = container.properties;
    if (properties == null || properties.topic == null || properties.topic.isBlank()) {
      ConfigWarnings.warnMissingTopicProperty(title);
      return null;
    }

    String topic = TopicPath.normalize(properties.topic);
    return switch (type) {
      case "Text Display" -> createDoubleEntry(topic, type, properties);
      case "Toggle Switch" -> createBooleanEntry(topic, type, properties);
      case "PIDController" -> createPidEntry(topic, properties);
      default -> {
        ConfigWarnings.warnUnsupportedWidget(title, type);
        yield null;
      }
    };
  }

  private static DoubleEntry createDoubleEntry(
      String topic, String widgetType, ElasticProperties properties) {
    if (properties.data_type != null
        && !properties.data_type.isBlank()
        && !"double".equals(properties.data_type)) {
      ConfigWarnings.warn(
          "Text Display at '"
              + topic
              + "' has data_type '"
              + properties.data_type
              + "'; only double is supported in this slice. Using double.");
    }
    double value = 0.0;
    if (properties.value != null && !properties.value.isNull()) {
      if (properties.value.isNumber()) {
        value = properties.value.asDouble();
      } else {
        ConfigWarnings.warnBadValue(topic, "expected a number");
      }
    }
    return new DoubleEntry(topic, widgetType, value);
  }

  private static BooleanEntry createBooleanEntry(
      String topic, String widgetType, ElasticProperties properties) {
    if (properties.data_type != null
        && !properties.data_type.isBlank()
        && !"boolean".equals(properties.data_type)) {
      ConfigWarnings.warn(
          "Toggle Switch at '"
              + topic
              + "' has data_type '"
              + properties.data_type
              + "'; expected boolean.");
    }
    boolean value = false;
    if (properties.value != null && !properties.value.isNull()) {
      if (properties.value.isBoolean()) {
        value = properties.value.asBoolean();
      } else {
        ConfigWarnings.warnBadValue(topic, "expected a boolean");
      }
    }
    return new BooleanEntry(topic, widgetType, value);
  }

  private static PIDControllerEntry createPidEntry(String topic, ElasticProperties properties) {
    double p = 0.0;
    double i = 0.0;
    double d = 0.0;
    double setpoint = 0.0;
    JsonNode value = properties.value;
    if (value != null && value.isObject()) {
      p = readChildDouble(value, "p", topic);
      i = readChildDouble(value, "i", topic);
      d = readChildDouble(value, "d", topic);
      if (value.has("setpoint")) {
        setpoint = readChildDouble(value, "setpoint", topic);
      }
    } else if (value != null && !value.isNull()) {
      ConfigWarnings.warnBadValue(topic, "PIDController value must be an object with p/i/d");
    }
    return new PIDControllerEntry(topic, p, i, d, setpoint);
  }

  private static double readChildDouble(JsonNode object, String field, String topic) {
    JsonNode child = object.get(field);
    if (child == null || child.isNull()) {
      return 0.0;
    }
    if (!child.isNumber()) {
      ConfigWarnings.warnBadValue(topic + "/" + field, "expected a number");
      return 0.0;
    }
    return child.asDouble();
  }
}
