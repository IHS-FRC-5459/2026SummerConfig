package org.team5459.config;

import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import edu.wpi.first.networktables.NetworkTableType;
import edu.wpi.first.networktables.NetworkTableValue;
import edu.wpi.first.networktables.Topic;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.team5459.config.types.BooleanNode;
import org.team5459.config.types.DoubleArrayNode;
import org.team5459.config.types.DoubleNode;
import org.team5459.config.types.FolderNode;
import org.team5459.config.types.IntArrayNode;
import org.team5459.config.types.IntNode;
import org.team5459.config.types.PIDControllerNode;
import org.team5459.config.types.ProfiledPIDControllerNode;
import org.team5459.config.types.StringNode;

/**
 * In debug mode, auto-registers NetworkTables topics under {@code /Config} that are not yet in the
 * typed document.
 *
 * <p>Supports Elastic Custom scalars retargeted to {@code /Config/...}, and PIDController tables
 * when Elastic writes {@code p}/{@code i}/{@code d}/{@code setpoint} or {@code .type=PIDController}
 * under a new path. Prefer {@link ConfigCreatePanel} for intentional creation.
 */
public final class ConfigDynamicRegistrar implements AutoCloseable {
  private static final String CONFIG_PREFIX = "/Config/";
  private static final Set<String> RESERVED_TOP_LEVEL =
      Set.of(
          ConfigSaveButton.kDefaultEntryName.toLowerCase(Locale.ROOT),
          ConfigDebugMode.kDefaultEntryName.toLowerCase(Locale.ROOT),
          ConfigCreatePanel.SUBTABLE.toLowerCase(Locale.ROOT),
          ConfigDeletePanel.SUBTABLE.toLowerCase(Locale.ROOT));
  private static final Set<String> PID_FIELDS = Set.of("p", "i", "d", "setpoint");

  private final ConfigDocument document;
  private final Runnable onRegistered;
  private final NetworkTableListener listener;
  private boolean notifying;
  private boolean pendingNotify;

  public ConfigDynamicRegistrar(ConfigDocument document, Runnable onRegistered) {
    this.document = document;
    this.onRegistered = onRegistered;
    this.listener =
        NetworkTableListener.createListener(
            NetworkTableInstance.getDefault(),
            new String[] {CONFIG_PREFIX, "/Config"},
            EnumSet.of(
                NetworkTableEvent.Kind.kPublish,
                NetworkTableEvent.Kind.kValueRemote,
                NetworkTableEvent.Kind.kValueLocal,
                NetworkTableEvent.Kind.kImmediate),
            this::onEvent);
  }

  /** Scans current {@code /Config} topics and registers any missing entries. */
  public void poll() {
    boolean registered = false;
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    for (Topic topic : inst.getTopics(CONFIG_PREFIX)) {
      NetworkTableValue value = inst.getEntry(topic.getName()).getValue();
      NetworkTableType type = topic.getType();
      if (type == NetworkTableType.kUnassigned && value != null) {
        type = value.getType();
      }
      registered |= considerTopic(topic.getName(), type, value, false);
    }
    if (registered) {
      notifyRegistered();
    }
  }

  private void onEvent(NetworkTableEvent event) {
    if (notifying) {
      return;
    }
    String topicName = null;
    NetworkTableType type = NetworkTableType.kUnassigned;
    NetworkTableValue value = null;

    if (event.valueData != null) {
      value = event.valueData.value;
      Topic topic = event.valueData.getTopic();
      if (topic != null) {
        topicName = topic.getName();
        type = topic.getType();
      }
      if (value != null && type == NetworkTableType.kUnassigned) {
        type = value.getType();
      }
    }
    if (topicName == null && event.topicInfo != null) {
      topicName = event.topicInfo.name;
      type = event.topicInfo.type;
    }

    if (considerTopic(topicName, type, value, false)) {
      notifyRegistered();
    }
  }

  private boolean considerTopic(
      String topicName, NetworkTableType type, NetworkTableValue value, boolean notify) {
    if (topicName == null) {
      return false;
    }

    String relativePath = toRelativePath(topicName);
    if (relativePath == null || isReserved(relativePath)) {
      return false;
    }
    if (ConfigDeletedPaths.isSuppressed(relativePath)) {
      return false;
    }

    if (value == null || type == NetworkTableType.kUnassigned) {
      NetworkTableValue current = NetworkTableInstance.getDefault().getEntry(topicName).getValue();
      if (current != null) {
        value = current;
        if (type == NetworkTableType.kUnassigned) {
          type = current.getType();
        }
      }
    }

    if (leafName(relativePath).equals(ConfigElasticTypes.TYPE_TOPIC)) {
      boolean created = tryRegisterFromTypeTopic(relativePath, value);
      if (created && notify) {
        notifyRegistered();
      }
      return created;
    }

    if (leafName(relativePath).startsWith(".")) {
      return false;
    }

    if (PID_FIELDS.contains(leafName(relativePath))
        && (type == NetworkTableType.kDouble
            || type == NetworkTableType.kFloat
            || type == NetworkTableType.kUnassigned)) {
      boolean created = tryRegisterPidFromField(relativePath);
      if (created && notify) {
        notifyRegistered();
      }
      return created;
    }

    if (document.hasPath(relativePath) || !canRegisterUnderFoldersOnly(relativePath)) {
      return false;
    }

    ConfigNode leaf = createLeaf(type, value);
    if (leaf == null) {
      return false;
    }

    if (!document.insertLeaf(relativePath, leaf)) {
      return false;
    }

    System.out.println(
        "[Config] Auto-registered /Config/" + relativePath + " as " + type.getValueStr());
    if (notify) {
      notifyRegistered();
    }
    return true;
  }

  private boolean tryRegisterFromTypeTopic(String typePath, NetworkTableValue value) {
    String parent = parentPath(typePath);
    if (parent == null || document.hasPath(parent) || !canRegisterUnderFoldersOnly(parent + "/p")) {
      return false;
    }
    String typeName = value != null ? value.getString() : "";
    if ("PIDController".equals(typeName)) {
      PIDControllerNode pid = buildPidNodeFromNt(parent);
      if (!document.insertLeaf(parent, pid)) {
        return false;
      }
      System.out.println("[Config] Auto-registered PIDController at /Config/" + parent);
      return true;
    }
    if ("ProfiledPIDController".equals(typeName)) {
      Map<String, ConfigNode> fields = new LinkedHashMap<>();
      fields.put("p", new DoubleNode(readDouble(parent + "/p", 0.0)));
      fields.put("i", new DoubleNode(readDouble(parent + "/i", 0.0)));
      fields.put("d", new DoubleNode(readDouble(parent + "/d", 0.0)));
      fields.put("maxVelocity", new DoubleNode(readDouble(parent + "/maxVelocity", 0.0)));
      fields.put("maxAcceleration", new DoubleNode(readDouble(parent + "/maxAcceleration", 0.0)));
      ProfiledPIDControllerNode node = new ProfiledPIDControllerNode(fields);
      if (!document.insertLeaf(parent, node)) {
        return false;
      }
      System.out.println("[Config] Auto-registered ProfiledPIDController at /Config/" + parent);
      return true;
    }
    return false;
  }

  private boolean tryRegisterPidFromField(String fieldPath) {
    String parent = parentPath(fieldPath);
    if (parent == null || document.hasPath(parent) || !canRegisterUnderFoldersOnly(parent + "/p")) {
      return false;
    }
    String type = readString(parent + "/" + ConfigElasticTypes.TYPE_TOPIC, "");
    boolean typed = "PIDController".equals(type);
    if (!typed) {
      boolean hasP = hasNtDouble(parent + "/p");
      boolean hasI = hasNtDouble(parent + "/i");
      boolean hasD = hasNtDouble(parent + "/d");
      if (!(hasP && (hasI || hasD))) {
        return false;
      }
    }

    PIDControllerNode pid = buildPidNodeFromNt(parent);
    if (!document.insertLeaf(parent, pid)) {
      return false;
    }
    System.out.println("[Config] Auto-registered PIDController at /Config/" + parent);
    return true;
  }

  private PIDControllerNode buildPidNodeFromNt(String parent) {
    Map<String, ConfigNode> fields = new LinkedHashMap<>();
    fields.put("p", new DoubleNode(readDouble(parent + "/p", 0.0)));
    fields.put("i", new DoubleNode(readDouble(parent + "/i", 0.0)));
    fields.put("d", new DoubleNode(readDouble(parent + "/d", 0.0)));
    fields.put("setpoint", new DoubleNode(readDouble(parent + "/setpoint", 0.0)));
    return new PIDControllerNode(fields);
  }

  private static double readDouble(String relativePath, double defaultValue) {
    NetworkTableValue value =
        NetworkTableInstance.getDefault().getEntry(CONFIG_PREFIX + relativePath).getValue();
    if (value == null || !value.isValid()) {
      return defaultValue;
    }
    try {
      return value.getDouble();
    } catch (RuntimeException exception) {
      return defaultValue;
    }
  }

  private static String readString(String relativePath, String defaultValue) {
    NetworkTableValue value =
        NetworkTableInstance.getDefault().getEntry(CONFIG_PREFIX + relativePath).getValue();
    if (value == null || !value.isValid()) {
      return defaultValue;
    }
    try {
      return value.getString();
    } catch (RuntimeException exception) {
      return defaultValue;
    }
  }

  private static boolean hasNtDouble(String relativePath) {
    NetworkTableValue value =
        NetworkTableInstance.getDefault().getEntry(CONFIG_PREFIX + relativePath).getValue();
    return value != null && value.isValid() && value.getType() == NetworkTableType.kDouble;
  }

  private boolean canRegisterUnderFoldersOnly(String relativePath) {
    String[] parts = relativePath.split("/");
    StringBuilder prefix = new StringBuilder();
    for (int index = 0; index < parts.length - 1; index++) {
      if (index > 0) {
        prefix.append('/');
      }
      prefix.append(parts[index]);
      ConfigNode ancestor = document.getNodeQuiet(prefix.toString());
      if (ancestor == null) {
        continue;
      }
      if (!(ancestor instanceof FolderNode)) {
        return false;
      }
    }
    return true;
  }

  private void notifyRegistered() {
    if (notifying) {
      pendingNotify = true;
      return;
    }
    notifying = true;
    try {
      do {
        pendingNotify = false;
        onRegistered.run();
      } while (pendingNotify);
    } finally {
      notifying = false;
    }
  }

  private static String toRelativePath(String topicName) {
    if (topicName.startsWith(CONFIG_PREFIX)) {
      String relative = topicName.substring(CONFIG_PREFIX.length());
      return relative.isBlank() || relative.endsWith("/") ? null : relative;
    }
    return null;
  }

  private static String parentPath(String relativePath) {
    int slash = relativePath.lastIndexOf('/');
    if (slash <= 0) {
      return null;
    }
    return relativePath.substring(0, slash);
  }

  private static String leafName(String relativePath) {
    int slash = relativePath.lastIndexOf('/');
    return slash < 0 ? relativePath : relativePath.substring(slash + 1);
  }

  private static boolean isReserved(String relativePath) {
    String top = relativePath.split("/", 2)[0];
    return RESERVED_TOP_LEVEL.contains(top.toLowerCase(Locale.ROOT));
  }

  private static ConfigNode createLeaf(NetworkTableType type, NetworkTableValue value) {
    return switch (type) {
      case kDouble, kFloat -> new DoubleNode(value != null ? value.getDouble() : 0.0);
      case kInteger -> new IntNode(value != null ? (int) value.getInteger() : 0);
      case kBoolean -> new BooleanNode(value != null && value.getBoolean());
      case kString -> new StringNode(value != null ? value.getString() : "");
      case kDoubleArray, kFloatArray -> new DoubleArrayNode(
          value != null ? value.getDoubleArray() : new double[0]);
      case kIntegerArray -> new IntArrayNode(toIntArray(value));
      default -> null;
    };
  }

  private static int[] toIntArray(NetworkTableValue value) {
    if (value == null) {
      return new int[0];
    }
    double[] published = value.getDoubleArray();
    int[] converted = new int[published.length];
    for (int i = 0; i < published.length; i++) {
      converted[i] = (int) published[i];
    }
    return converted;
  }

  @Override
  public void close() {
    listener.close();
  }
}
