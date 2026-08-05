package org.team5459.config;

import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import java.util.EnumSet;

/**
 * Momentary NetworkTables Save control that promotes the live debug document into {@code
 * robot-config.json}.
 *
 * <p>The entry (default {@code Config/Save}) resets to {@code false} after each press. Use a toggle
 * or button widget on the dashboard.
 */
public final class ConfigSaveButton {
  public static final String kDefaultTableName = "Config";
  public static final String kDefaultEntryName = "Save";

  private ConfigSaveButton() {}

  /** Ensures the Save entry exists and is {@code false}. */
  public static void publish(String tableName, String entryName) {
    NetworkTableInstance.getDefault().getTable(tableName).getEntry(entryName).setBoolean(false);
  }

  /**
   * Listens for Save presses and runs {@code onSave}.
   *
   * @return listener to close on shutdown
   */
  public static NetworkTableListener listen(String tableName, String entryName, Runnable onSave) {
    var saveEntry = NetworkTableInstance.getDefault().getTable(tableName).getEntry(entryName);
    saveEntry.setBoolean(false);
    return NetworkTableListener.createListener(
        saveEntry,
        EnumSet.of(NetworkTableEvent.Kind.kValueRemote),
        event -> {
          if (event.valueData != null && event.valueData.value.getBoolean()) {
            onSave.run();
            saveEntry.setBoolean(false);
          }
        });
  }
}
