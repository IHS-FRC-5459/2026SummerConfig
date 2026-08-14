package org.team5459.config;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import java.util.Map;
import org.team5459.config.types.BooleanNode;
import org.team5459.config.types.DoubleArrayNode;
import org.team5459.config.types.DoubleNode;
import org.team5459.config.types.FolderNode;
import org.team5459.config.types.IntArrayNode;
import org.team5459.config.types.IntNode;
import org.team5459.config.types.StringNode;

/**
 * One-shot pull of current NetworkTables values into a typed document.
 *
 * <p>Used when entering debug mode so getters immediately reflect whatever the dashboard already
 * published, without waiting for a new remote edit event.
 */
public final class TypedNetworkTablePull {

  private TypedNetworkTablePull() {}

  public static void pull(ConfigDocument document) {
    NetworkTable table = NetworkTableInstance.getDefault().getTable("Config");
    pullEntries(document.getRootEntries(), table);
  }

  private static void pullEntries(Map<String, ConfigNode> entries, NetworkTable table) {
    entries.forEach((name, node) -> pullNode(node, name, table));
  }

  private static void pullNode(ConfigNode node, String name, NetworkTable table) {
    if (node instanceof FolderNode folder) {
      pullEntries(folder.getChildren(), table.getSubTable(name));
    } else if (node instanceof CompositeConfigNode composite) {
      pullEntries(composite.getFields(), table.getSubTable(name));
      composite.applyFieldChanges();
    } else if (node instanceof DoubleNode doubleNode) {
      NetworkTableEntry entry = table.getEntry(name);
      if (entry.exists()) {
        doubleNode.setValue(entry.getDouble(doubleNode.getValue()));
      }
    } else if (node instanceof IntNode intNode) {
      NetworkTableEntry entry = table.getEntry(name);
      if (entry.exists()) {
        intNode.setValue((int) entry.getInteger(intNode.getValue()));
      }
    } else if (node instanceof BooleanNode booleanNode) {
      NetworkTableEntry entry = table.getEntry(name);
      if (entry.exists()) {
        booleanNode.setValue(entry.getBoolean(booleanNode.getValue()));
      }
    } else if (node instanceof StringNode stringNode) {
      NetworkTableEntry entry = table.getEntry(name);
      if (entry.exists()) {
        stringNode.setValue(entry.getString(stringNode.getValue()));
      }
    } else if (node instanceof DoubleArrayNode arrayNode) {
      NetworkTableEntry entry = table.getEntry(name);
      if (entry.exists()) {
        arrayNode.setValue(entry.getDoubleArray(arrayNode.getValue()));
      }
    } else if (node instanceof IntArrayNode arrayNode) {
      NetworkTableEntry entry = table.getEntry(name);
      if (entry.exists()) {
        double[] published = entry.getDoubleArray(toDoubleArray(arrayNode.getValue()));
        arrayNode.setValue(fromDoubleArray(published));
      }
    }
  }

  private static double[] toDoubleArray(int[] values) {
    double[] converted = new double[values.length];
    for (int index = 0; index < values.length; index++) {
      converted[index] = values[index];
    }
    return converted;
  }

  private static int[] fromDoubleArray(double[] values) {
    int[] converted = new int[values.length];
    for (int index = 0; index < values.length; index++) {
      converted[index] = (int) values[index];
    }
    return converted;
  }
}
