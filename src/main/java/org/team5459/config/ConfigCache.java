package org.team5459.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.team5459.config.entries.BooleanEntry;
import org.team5459.config.entries.ConfigEntry;
import org.team5459.config.entries.DoubleEntry;
import org.team5459.config.entries.PIDControllerEntry;

/**
 * Reads/writes {@code config-cache.json}: a topic-keyed snapshot of live tunable values.
 *
 * <p>Elastic Save As may omit {@code properties.value}; the cache is how tuned numbers survive
 * reboot on the robot until they are promoted into git by some other workflow.
 */
public final class ConfigCache {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  private ConfigCache() {}

  /** Overlays cached values onto an already-loaded document. Missing cache file is a no-op. */
  public static void apply(File cacheFile, ConfigDocument document) {
    if (cacheFile == null || !cacheFile.isFile()) {
      return;
    }
    try {
      JsonNode root = MAPPER.readTree(cacheFile);
      if (root == null || !root.isObject()) {
        ConfigWarnings.warn("Config cache is not a JSON object: " + cacheFile);
        return;
      }
      for (Map.Entry<String, ConfigEntry> entry : document.getEntries().entrySet()) {
        String topic = entry.getKey();
        JsonNode cached = root.get(topic);
        if (cached == null || cached.isNull()) {
          continue;
        }
        applyValue(entry.getValue(), cached, topic);
      }
      System.out.println("Applied config cache from " + cacheFile.getAbsolutePath());
    } catch (IOException exception) {
      ConfigWarnings.warn(
          "Failed to read config cache " + cacheFile + ": " + exception.getMessage());
    }
  }

  /** Writes current document values to the cache file (creates parent dirs if needed). */
  public static void save(File cacheFile, ConfigDocument document) {
    if (cacheFile == null) {
      return;
    }
    try {
      File parent = cacheFile.getParentFile();
      if (parent != null) {
        parent.mkdirs();
      }
      ObjectNode root = MAPPER.createObjectNode();
      for (ConfigEntry entry : document.getEntries().values()) {
        root.set(entry.topic(), toJson(entry));
      }
      MAPPER.writeValue(cacheFile, root);
    } catch (IOException exception) {
      ConfigWarnings.warn(
          "Failed to write config cache " + cacheFile + ": " + exception.getMessage());
    }
  }

  private static void applyValue(ConfigEntry entry, JsonNode cached, String topic) {
    if (entry instanceof DoubleEntry doubleEntry) {
      double value = readNumber(cached, "value", topic);
      if (!Double.isNaN(value)) {
        doubleEntry.setValue(value);
      }
    } else if (entry instanceof BooleanEntry booleanEntry) {
      Boolean value = readBoolean(cached, "value", topic);
      if (value != null) {
        booleanEntry.setValue(value);
      }
    } else if (entry instanceof PIDControllerEntry pidEntry) {
      double p = readNumber(cached, "p", topic);
      double i = readNumber(cached, "i", topic);
      double d = readNumber(cached, "d", topic);
      double setpoint = readNumber(cached, "setpoint", topic);
      if (!Double.isNaN(p)) {
        pidEntry.getController().setP(p);
      }
      if (!Double.isNaN(i)) {
        pidEntry.getController().setI(i);
      }
      if (!Double.isNaN(d)) {
        pidEntry.getController().setD(d);
      }
      if (!Double.isNaN(setpoint)) {
        pidEntry.getController().setSetpoint(setpoint);
      }
    }
  }

  private static JsonNode toJson(ConfigEntry entry) {
    ObjectNode node = MAPPER.createObjectNode();
    node.put("widgetType", entry.widgetType());
    if (entry instanceof DoubleEntry doubleEntry) {
      node.put("value", doubleEntry.getValue());
    } else if (entry instanceof BooleanEntry booleanEntry) {
      node.put("value", booleanEntry.getValue());
    } else if (entry instanceof PIDControllerEntry pidEntry) {
      node.put("p", pidEntry.getP());
      node.put("i", pidEntry.getI());
      node.put("d", pidEntry.getD());
      node.put("setpoint", pidEntry.getSetpoint());
    }
    return node;
  }

  private static double readNumber(JsonNode cached, String field, String topic) {
    JsonNode node = cached.get(field);
    if (node == null || node.isNull()) {
      // Allow bare numeric cache values for scalars
      if ("value".equals(field) && cached.isNumber()) {
        return cached.asDouble();
      }
      return Double.NaN;
    }
    if (!node.isNumber()) {
      ConfigWarnings.warnBadValue(topic + "/" + field, "expected a number in config cache");
      return Double.NaN;
    }
    return node.asDouble();
  }

  private static Boolean readBoolean(JsonNode cached, String field, String topic) {
    JsonNode node = cached.get(field);
    if (node == null || node.isNull()) {
      if ("value".equals(field) && cached.isBoolean()) {
        return cached.asBoolean();
      }
      return null;
    }
    if (!node.isBoolean()) {
      ConfigWarnings.warnBadValue(topic + "/" + field, "expected a boolean in config cache");
      return null;
    }
    return node.asBoolean();
  }
}
