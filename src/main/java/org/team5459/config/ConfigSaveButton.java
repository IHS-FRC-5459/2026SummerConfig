package org.team5459.config;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import java.util.EnumSet;

/**
 * Momentary NetworkTables Save control that promotes the live debug document into {@code
 * robot-config.json}.
 *
 * <p>Listens for {@code kValueRemote} only so Elastic Toggle Button presses are detected, while the
 * robot clearing Save back to {@code false} does not re-trigger.
 */
public final class ConfigSaveButton implements AutoCloseable {
  public static final String kDefaultTableName = "Config";
  public static final String kDefaultEntryName = "Save";

  private final String tableName;
  private final String entryName;
  private final Runnable onSave;
  private final NetworkTableListener listener;

  public ConfigSaveButton(String tableName, String entryName, Runnable onSave) {
    this.tableName = tableName;
    this.entryName = entryName;
    this.onSave = onSave;
    NetworkTableEntry saveEntry = entry();
    saveEntry.setBoolean(false);
    this.listener =
        NetworkTableListener.createListener(
            saveEntry,
            EnumSet.of(NetworkTableEvent.Kind.kValueRemote),
            event -> {
              if (event.valueData == null
                  || event.valueData.value == null
                  || !event.valueData.value.getBoolean()) {
                return;
              }
              saveEntry.setBoolean(false);
              System.out.println("[Config] Save pressed — promoting to robot-config.json");
              if (this.onSave != null) {
                this.onSave.run();
              }
            });
  }

  /** Ensures the Save entry exists and is {@code false}. */
  public void publish() {
    entry().setBoolean(false);
  }

  /**
   * Optional periodic clear of a sticky {@code true} left on the wire. Prefer the remote listener
   * for press detection.
   */
  public boolean poll() {
    NetworkTableEntry saveEntry = entry();
    if (saveEntry.getBoolean(false)) {
      // Remote listener should have handled a fresh press; clear sticky leftovers only.
      saveEntry.setBoolean(false);
    }
    return false;
  }

  private NetworkTableEntry entry() {
    return NetworkTableInstance.getDefault().getTable(tableName).getEntry(entryName);
  }

  @Override
  public void close() {
    listener.close();
    entry().setBoolean(false);
  }
}
