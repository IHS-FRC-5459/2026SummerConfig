package org.team5459.config;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import edu.wpi.first.networktables.NetworkTableType;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Momentary dashboard boolean (Elastic Toggle Button / Switch).
 *
 * <p>The robot publishes a typed {@code boolean} with {@link NetworkTableEntry#setBoolean(boolean)}
 * so Elastic can write via {@code updateDataFromTopic}.
 *
 * <p>Toggle Button clicks can be shorter than one robot period (unlike sticky Toggle Switch). A
 * NetworkTables value listener latches remote/local {@code true} so brief presses are not missed by
 * rising-edge polling alone.
 */
final class ConfigDashboardPulse implements AutoCloseable {
  private final String label;
  private final NetworkTableEntry entry;
  private final NetworkTableListener valueListener;
  private final AtomicBoolean latchedTrue = new AtomicBoolean(false);
  private final AtomicBoolean suppressingClear = new AtomicBoolean(false);
  private boolean lastValue;
  private long lastSeenChange;

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
    this.lastSeenChange = entry.getLastChange();
    this.valueListener =
        NetworkTableListener.createListener(
            entry,
            EnumSet.of(
                NetworkTableEvent.Kind.kValueRemote, NetworkTableEvent.Kind.kValueLocal),
            event -> {
              if (suppressingClear.get()
                  || event.valueData == null
                  || event.valueData.value == null) {
                return;
              }
              if (event.valueData.value.getBoolean()) {
                latchedTrue.set(true);
                System.out.println(
                    "[Config][Pulse] "
                        + label
                        + " latched TRUE ("
                        + event.is(NetworkTableEvent.Kind.kValueRemote)
                        + " remote) lastChange="
                        + entry.getLastChange());
              }
            });
    System.out.println(
        "[Config][Pulse] publish-typed "
            + label
            + " initial="
            + lastValue
            + " exists="
            + entry.exists()
            + " type="
            + entry.getType()
            + " lastChange="
            + lastSeenChange);
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
   * @return {@code true} once when a dashboard press is detected
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

    NetworkTableInstance.getDefault().waitForListenerQueue(0.0);

    boolean pressed = latchedTrue.getAndSet(false);
    boolean value = entry.getBoolean(false);
    long change = entry.getLastChange();

    if (change != lastSeenChange) {
      System.out.println(
          "[Config][Pulse] "
              + label
              + " lastChange "
              + lastSeenChange
              + " -> "
              + change
              + " val="
              + value
              + " type="
              + entry.getType());
      lastSeenChange = change;
    }

    if (value != lastValue) {
      System.out.println(
          "[Config][Pulse] "
              + label
              + " level "
              + lastValue
              + " -> "
              + value
              + " type="
              + entry.getType());
    }

    if (value && !lastValue) {
      pressed = true;
    }

    if (pressed) {
      System.out.println("[Config][Pulse] " + label + " PRESS — handling");
      suppressingClear.set(true);
      try {
        entry.setBoolean(false);
      } finally {
        suppressingClear.set(false);
      }
      lastValue = false;
      lastSeenChange = entry.getLastChange();
      latchedTrue.set(false);
      return true;
    }

    lastValue = value;
    return false;
  }

  void clearToFalse() {
    suppressingClear.set(true);
    try {
      entry.setBoolean(false);
    } finally {
      suppressingClear.set(false);
    }
    lastValue = false;
    latchedTrue.set(false);
    lastSeenChange = entry.getLastChange();
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
        + " lastChange="
        + entry.getLastChange()
        + " latched="
        + latchedTrue.get()
        + "}";
  }

  @Override
  public void close() {
    clearToFalse();
    valueListener.close();
  }
}
