package org.team5459.config;

import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import java.io.File;
import java.util.EnumSet;

/**
 * Published a momentary "Save" button to the NetworkTables that commits a {@link ConfigDocument} to
 * a file when pressed.
 *
 * <p>Intended to pair with {@link TypedNetworkTableSync#listen(ConfigDocument, Runnable)}: bind the
 * sync listener's {@code onUpdate} callback to save into a scratch/cache file for live tuning, then
 * use this class to commit those in-memory values to the real config file only when a human
 * explicitly presses Save.
 *
 * <p>The underlying NetwordTables entry (default {@Config/Save}) is reset back to {@code false}
 * immediately after each press, so it behaves as a momentary trigger regardless of whether the
 * dashboard widget bound to it is a button or a toggle switch (CANNOT BE A BOOLEAN MUST ALWAYS
 * SWITCH TO A TOGGLE OR BUTTON TO WORK).
 */
public final class ConfigSaveButton {
  static final String kDefaultTableName = "Config";
  static final String kDefaultEntryName = "Save";

  private ConfigSaveButton() {}

  /**
   * Creates a save-button listener under the default {@code ConfigManager/Save} entry.
   *
   * @param configFile destination file to write on each press
   * @param document configuration document to save
   * @return the created listener; close it when no longer needed
   */
  public static NetworkTableListener listen(File configFile, ConfigDocument document) {
    return listen(kDefaultTableName, kDefaultEntryName, configFile, document);
  }

  /**
   * Creates a save-button listener under a custom table/entry name.
   *
   * @param tableName NetworkTables table to publish the button under
   * @param entryName entry name within that table
   * @param configFile destination file to write on each press
   * @param document configuration document to save
   * @return the created listener; close it when no longer needed
   */
  public static NetworkTableListener listen(
      String tableName, String entryName, File configFile, ConfigDocument document) {
    var saveEntry = NetworkTableInstance.getDefault().getTable(tableName).getEntry(entryName);
    saveEntry.setBoolean(false);
    return NetworkTableListener.createListener(
        saveEntry,
        EnumSet.of(NetworkTableEvent.Kind.kValueRemote),
        event -> {
          if (event.valueData != null && event.valueData.value.getBoolean()) {
            TypedConfigSaver.save(configFile, document);
            System.out.println("Committed config: " + configFile.getAbsolutePath());
            saveEntry.setBoolean(false);
          }
        });
  }
}
