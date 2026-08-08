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
 * remote NetworkTables value listener latches {@code true} so brief Elastic presses are not missed
 * by rising-edge polling alone.
 */
final class ConfigDashboardPulse implements AutoCloseable {
  private final NetworkTableEntry entry;
  private final NetworkTableListener valueListener;
  private final AtomicBoolean latchedTrue = new AtomicBoolean(false);
  private final AtomicBoolean suppressingClear = new AtomicBoolean(false);
  private boolean lastValue;

  ConfigDashboardPulse(String fullTopicName) {
    this(fullTopicName, fullTopicName);
  }

  ConfigDashboardPulse(String fullTopicName, String label) {
    String path = fullTopicName.startsWith("/") ? fullTopicName.substring(1) : fullTopicName;
    String[] parts = path.split("/");
    var table = NetworkTableInstance.getDefault().getTable(parts[0]);
    for (int i = 1; i < parts.length - 1; i++) {
      table = table.getSubTable(parts[i]);
    }
    this.entry = table.getEntry(parts[parts.length - 1]);
    ensureTypedBoolean();
    this.valueListener =
        NetworkTableListener.createListener(
            entry,
            EnumSet.of(NetworkTableEvent.Kind.kValueRemote),
            event -> {
              if (suppressingClear.get()
                  || event.valueData == null
                  || event.valueData.value == null) {
                return;
              }
              if (event.valueData.value.getBoolean()) {
                latchedTrue.set(true);
              }
            });
  }

  /** Re-publish as a typed boolean if the topic was left kUnassigned or unpublished. */
  void ensureTypedBoolean() {
    boolean value = entry.getType() == NetworkTableType.kBoolean ? entry.getBoolean(false) : false;
    entry.setBoolean(value);
    lastValue = entry.getBoolean(false);
  }

  /**
   * @return {@code true} once when a dashboard press is detected
   */
  boolean pollRisingEdge() {
    if (entry.getType() != NetworkTableType.kBoolean) {
      ensureTypedBoolean();
    }

    NetworkTableInstance.getDefault().waitForListenerQueue(0.0);

    boolean pressed = latchedTrue.getAndSet(false);
    boolean value = entry.getBoolean(false);

    if (value && !lastValue) {
      pressed = true;
    }

    if (pressed) {
      suppressingClear.set(true);
      try {
        entry.setBoolean(false);
      } finally {
        suppressingClear.set(false);
      }
      lastValue = false;
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
  }

  @Override
  public void close() {
    clearToFalse();
    valueListener.close();
  }
}
