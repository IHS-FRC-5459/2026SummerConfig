package org.team5459.config;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.team5459.config.types.*;

/**
 * Synchronizes a typed {@link ConfigDocument} with NetworkTables.
 *
 * <p>Folder and composite entries publish as subtables under {@code /Config}. Scalar and array
 * entries publish as individual NT values inside their parent table. The table layout therefore
 * mirrors JSON path structure: {@code Config/Arm/PIDController/p}.
 *
 * <p>Composites that match an Elastic multi-topic widget (for example {@code PIDController}) also
 * publish a Sendable-style {@code .type} string so Elastic can offer the correct widget. See {@link
 * ConfigElasticTypes}.
 *
 * <p>{@link #listen(ConfigDocument, Runnable)} attaches {@code kValueRemote} listeners to editable
 * scalar leaves. When a dashboard writes a new value, the in-memory node is updated and parent
 * composites call {@link CompositeConfigNode#applyFieldChanges()}. The optional {@code onUpdate}
 * callback runs after each applied edit (typically to save JSON back to disk).
 *
 * <p>Composite and folder nodes themselves are not directly editable over NetworkTables; only their
 * scalar/array descendants are (plus PID {@code setpoint} for Elastic's PID widget).
 */
public final class TypedNetworkTableSync {

  private static final EnumSet<NetworkTableEvent.Kind> REMOTE_VALUE_EVENTS =
      EnumSet.of(NetworkTableEvent.Kind.kValueRemote, NetworkTableEvent.Kind.kValueLocal);

  private TypedNetworkTableSync() {}

  /** Publishes every entry in the document under the {@code Config} table. */
  public static void publish(ConfigDocument document) {
    NetworkTable table = NetworkTableInstance.getDefault().getTable("Config");
    publishEntries(document.getRootEntries(), table);
  }

  /** Creates listeners that apply remote NetworkTables edits to the document. */
  public static NetworkTableListener[] listen(ConfigDocument document) {
    return listen(document, () -> {});
  }

  /** Creates listeners that apply remote NetworkTables edits to the document. */
  public static NetworkTableListener[] listen(ConfigDocument document, Runnable onUpdate) {
    List<NetworkTableListener> listeners = new ArrayList<>();
    NetworkTable table = NetworkTableInstance.getDefault().getTable("Config");
    listenEntries(document.getRootEntries(), table, onUpdate, listeners);
    return listeners.toArray(NetworkTableListener[]::new);
  }

  private static void publishEntries(Map<String, ConfigNode> entries, NetworkTable table) {
    // Snapshot so NT publish callbacks cannot ConcurrentModificationException the live map.
    for (Map.Entry<String, ConfigNode> entry : List.copyOf(entries.entrySet())) {
      publishNode(entry.getValue(), entry.getKey(), table);
    }
  }

  private static void publishNode(ConfigNode node, String name, NetworkTable table) {
    if (node instanceof FolderNode folder) {
      publishEntries(folder.getChildren(), table.getSubTable(name));
    } else if (node instanceof CompositeConfigNode composite) {
      NetworkTable subTable = table.getSubTable(name);
      publishEntries(composite.getFields(), subTable);
      publishElasticMetadata(composite, subTable);
    } else if (node instanceof DoubleNode doubleNode) {
      table.getEntry(name).setDouble(doubleNode.getValue());
    } else if (node instanceof IntNode intNode) {
      table.getEntry(name).setInteger(intNode.getValue());
    } else if (node instanceof BooleanNode booleanNode) {
      table.getEntry(name).setBoolean(booleanNode.getValue());
    } else if (node instanceof StringNode stringNode) {
      table.getEntry(name).setString(stringNode.getValue());
    } else if (node instanceof DoubleArrayNode arrayNode) {
      table.getEntry(name).setDoubleArray(arrayNode.getValue());
    } else if (node instanceof IntArrayNode arrayNode) {
      table.getEntry(name).setDoubleArray(toDoubleArray(arrayNode.getValue()));
    } else {
      ConfigWarnings.warnUnsupportedNetworkTablesType(name, node);
    }
  }

  /** Publishes {@code .type} (and PID setpoint) so Elastic can bind multi-topic widgets. */
  private static void publishElasticMetadata(CompositeConfigNode composite, NetworkTable subTable) {
    String elasticType = ConfigElasticTypes.elasticTypeFor(composite);
    if (elasticType != null) {
      subTable.getEntry(ConfigElasticTypes.TYPE_TOPIC).setString(elasticType);
    }
    if (composite instanceof PIDControllerNode pidNode) {
      subTable.getEntry("setpoint").setDouble(pidNode.getController().getSetpoint());
    }
  }

  private static void listenEntries(
      Map<String, ConfigNode> entries,
      NetworkTable table,
      Runnable onUpdate,
      List<NetworkTableListener> listeners) {
    entries.forEach((name, node) -> listenNode(node, name, table, onUpdate, listeners));
  }

  private static void listenNode(
      ConfigNode node,
      String name,
      NetworkTable table,
      Runnable onUpdate,
      List<NetworkTableListener> listeners) {
    if (node instanceof FolderNode folder) {
      listenEntries(folder.getChildren(), table.getSubTable(name), onUpdate, listeners);
    } else if (node instanceof CompositeConfigNode composite) {
      NetworkTable subTable = table.getSubTable(name);
      Runnable afterChildUpdate =
          () -> {
            composite.applyFieldChanges();
            onUpdate.run();
          };
      listenEntries(composite.getFields(), subTable, afterChildUpdate, listeners);
      if (composite instanceof PIDControllerNode pidNode) {
        listeners.add(createPidSetpointListener(subTable.getEntry("setpoint"), pidNode, onUpdate));
      }
    } else if (node instanceof DoubleNode doubleNode) {
      listeners.add(createDoubleListener(table.getEntry(name), doubleNode, onUpdate));
    } else if (node instanceof IntNode intNode) {
      listeners.add(createIntListener(table.getEntry(name), intNode, onUpdate));
    } else if (node instanceof BooleanNode booleanNode) {
      listeners.add(createBooleanListener(table.getEntry(name), booleanNode, onUpdate));
    } else if (node instanceof StringNode stringNode) {
      listeners.add(createStringListener(table.getEntry(name), stringNode, onUpdate));
    } else if (node instanceof DoubleArrayNode arrayNode) {
      listeners.add(createDoubleArrayListener(table.getEntry(name), arrayNode, onUpdate));
    } else if (node instanceof IntArrayNode arrayNode) {
      listeners.add(createIntArrayListener(table.getEntry(name), arrayNode, onUpdate));
    } else {
      ConfigWarnings.warnUnsupportedNetworkTablesType(name, node);
    }
  }

  private static NetworkTableListener createPidSetpointListener(
      NetworkTableEntry entry, PIDControllerNode pidNode, Runnable onUpdate) {
    entry.setDouble(pidNode.getController().getSetpoint());
    return NetworkTableListener.createListener(
        entry,
        REMOTE_VALUE_EVENTS,
        event -> {
          if (event.valueData == null) {
            return;
          }
          pidNode.getController().setSetpoint(event.valueData.value.getDouble());
          onUpdate.run();
        });
  }

  private static NetworkTableListener createDoubleListener(
      NetworkTableEntry entry, DoubleNode node, Runnable onUpdate) {
    entry.setDouble(node.getValue());
    return NetworkTableListener.createListener(
        entry,
        REMOTE_VALUE_EVENTS,
        event -> {
          if (event.valueData == null) {
            return;
          }
          node.setValue(event.valueData.value.getDouble());
          onUpdate.run();
        });
  }

  private static NetworkTableListener createIntListener(
      NetworkTableEntry entry, IntNode node, Runnable onUpdate) {
    entry.setInteger(node.getValue());
    return NetworkTableListener.createListener(
        entry,
        REMOTE_VALUE_EVENTS,
        event -> {
          if (event.valueData == null) {
            return;
          }
          node.setValue((int) event.valueData.value.getInteger());
          onUpdate.run();
        });
  }

  private static NetworkTableListener createBooleanListener(
      NetworkTableEntry entry, BooleanNode node, Runnable onUpdate) {
    entry.setBoolean(node.getValue());
    return NetworkTableListener.createListener(
        entry,
        REMOTE_VALUE_EVENTS,
        event -> {
          if (event.valueData == null) {
            return;
          }
          node.setValue(event.valueData.value.getBoolean());
          onUpdate.run();
        });
  }

  private static NetworkTableListener createStringListener(
      NetworkTableEntry entry, StringNode node, Runnable onUpdate) {
    entry.setString(node.getValue());
    return NetworkTableListener.createListener(
        entry,
        REMOTE_VALUE_EVENTS,
        event -> {
          if (event.valueData == null) {
            return;
          }
          node.setValue(event.valueData.value.getString());
          onUpdate.run();
        });
  }

  private static NetworkTableListener createDoubleArrayListener(
      NetworkTableEntry entry, DoubleArrayNode node, Runnable onUpdate) {
    entry.setDoubleArray(node.getValue());
    return NetworkTableListener.createListener(
        entry,
        REMOTE_VALUE_EVENTS,
        event -> {
          if (event.valueData == null) {
            return;
          }
          node.setValue(event.valueData.value.getDoubleArray());
          onUpdate.run();
        });
  }

  private static NetworkTableListener createIntArrayListener(
      NetworkTableEntry entry, IntArrayNode node, Runnable onUpdate) {
    entry.setDoubleArray(toDoubleArray(node.getValue()));
    return NetworkTableListener.createListener(
        entry,
        REMOTE_VALUE_EVENTS,
        event -> {
          if (event.valueData == null) {
            return;
          }
          node.setValue(fromDoubleArray(event.valueData.value.getDoubleArray()));
          onUpdate.run();
        });
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
