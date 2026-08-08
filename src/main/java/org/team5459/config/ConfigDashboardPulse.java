package org.team5459.config;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableType;

/**
 * Momentary dashboard boolean (Elastic Toggle Button / Switch).
 *
 * <p>The robot publishes a typed {@code boolean} with {@link NetworkTableEntry#setBoolean(boolean)}
 * so Elastic skips {@code publishTopic} and only calls {@code updateDataFromTopic}.
 *
 * <p>Do <strong>not</strong> subscribe-only first: that creates {@code kUnassigned} topics that
 * block Elastic publishes while never carrying a boolean value.
 */
final class ConfigDashboardPulse {
  private final String label;
  private final NetworkTableEntry entry;
  private boolean lastValue;

  ConfigDashboardPulse(String fullTopicName) {
    this(fullTopicName, fullTopicName);
  }

  ConfigDashboardPulse(String fullTopicName, String label) {
    this.label = label;
    String path = fullTopicName.startsWith("/") ? fullTopicName.substring(1) : fullTopicName;
    String[] parts = path.split("/");
    var table = NetworkTableInstance.getDefault().getTable(parts[0]);
    for (int i = 1; i < parts.length - 1; i++) {
      table = table.getSubTable(parts[i]);
    }
    this.entry = table.getEntry(parts[parts.length - 1]);
    ensureTypedBoolean();
    System.out.println(
        "[Config][Pulse] publish-typed "
            + label
            + " initial="
            + lastValue
            + " exists="
            + entry.exists()
            + " type="
            + entry.getType());
  }

  String label() {
    return label;
  }

  /** Re-publish as a typed boolean if the topic was left kUnassigned or unpublished. */
  void ensureTypedBoolean() {
    NetworkTableType before = entry.getType();
    boolean value = before == NetworkTableType.kBoolean ? entry.getBoolean(false) : false;
    entry.setBoolean(value);
    lastValue = entry.getBoolean(false);
    NetworkTableType after = entry.getType();
    if (before != after || after != NetworkTableType.kBoolean) {
      System.out.println(
          "[Config][Pulse] ensureTypedBoolean "
              + label
              + " before="
              + before
              + " after="
              + after
              + " val="
              + lastValue
              + " exists="
              + entry.exists());
    }
  }

  boolean currentValue() {
    return entry.getBoolean(false);
  }

  boolean topicExists() {
    return entry.exists();
  }

  /**
   * @return {@code true} once when the dashboard value rises to {@code true}
   */
  boolean pollRisingEdge() {
    if (entry.getType() != NetworkTableType.kBoolean) {
      System.out.println(
          "[Config][Pulse] "
              + label
              + " not kBoolean (type="
              + entry.getType()
              + " exists="
              + entry.exists()
              + ") — re-publishing typed boolean");
      ensureTypedBoolean();
    }
    boolean value = entry.getBoolean(false);

    if (value != lastValue) {
      System.out.println(
          "[Config][Pulse] "
              + label
              + " changed "
              + lastValue
              + " -> "
              + value
              + " exists="
              + entry.exists()
              + " type="
              + entry.getType());
    }

    boolean rising = value && !lastValue;
    if (rising) {
      System.out.println("[Config][Pulse] " + label + " RISING EDGE — handling press");
      entry.setBoolean(false);
      boolean cleared = entry.getBoolean(false);
      if (cleared) {
        System.out.println(
            "[Config][Pulse] "
                + label
                + " WARNING: still true after clear (Elastic may be winning publish fight)");
      }
      lastValue = false;
      return true;
    }
    lastValue = value;
    return false;
  }

  void clearToFalse() {
    entry.setBoolean(false);
    lastValue = false;
  }

  String statusLine() {
    return label
        + "{val="
        + entry.getBoolean(false)
        + " last="
        + lastValue
        + " exists="
        + entry.exists()
        + " type="
        + entry.getType()
        + "}";
  }

  void close() {
    clearToFalse();
  }
}
