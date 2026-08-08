package org.team5459.config;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * Momentary dashboard boolean (Elastic Toggle Button / Switch).
 *
 * <p>Matches the working {@link ConfigDebugMode} pattern: the robot publishes a typed {@code
 * boolean} with {@link NetworkTableEntry#setBoolean(boolean)}. Elastic then sees the topic as
 * already published, skips {@code publishTopic} (which throws on its unmodifiable map), and only
 * calls {@code updateDataFromTopic} — so presses reach the robot.
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
    // fullTopicName like "/Config/Save" or "/Config/Create/Go"
    String path = fullTopicName.startsWith("/") ? fullTopicName.substring(1) : fullTopicName;
    String[] parts = path.split("/");
    var table = NetworkTableInstance.getDefault().getTable(parts[0]);
    for (int i = 1; i < parts.length - 1; i++) {
      table = table.getSubTable(parts[i]);
    }
    this.entry = table.getEntry(parts[parts.length - 1]);
    // Publish a real boolean so Elastic can updateDataFromTopic without publishTopic.
    this.entry.setBoolean(false);
    this.lastValue = this.entry.getBoolean(false);
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
