package org.team5459.config;

import edu.wpi.first.networktables.NetworkTableListener;
import edu.wpi.first.wpilibj.DriverStation;
import java.io.File;

/**
 * Owns the full lifecycle of a deploy-backed typed configuration: loading from disk, publishing to
 * NetworkTables, live-tuning autosave to a scratch cache file, and a dashboard Save button that
 * commits the cache into the real config file.
 *
 * <p>Typical usage from {@code Robot.robotInit()}:
 *
 * <pre>{@code
 * private ConfigManager configManager;
 *
 * public void robotInit(){
 *   configManager = new ConfigManager(
 *     new File(Filesystem.getDeployDirectory(), "robot-config.json"),
 *   new File(Filesystem.getDeployDirectory(), "config-cache.json"));}
 * }</pre>
 *
 * <p>Values tuned live from a dashboard (e.g. Elastic) autosave into the cache file immediately.
 * Pressing the Save button (default topic {@code Config/Save}) commits the current in-memory values
 * into the real config file. Robot code should read tuned values through {@link #getDocument()}
 */
public class ConfigManager {
  private final File configFile;
  private final File cacheFile;
  private final ConfigDocument document;
  private final NetworkTableListener[] configListeners;
  private final NetworkTableListener saveButtonListener;

  /**
   * Loads {@code configFile}, publishes it to NetworkTables, and wires up cache autosave plus the
   * Save button under a custom table/entry name.
   *
   * @param configFile the committed config file, loaded at startup and written on Save
   * @param cacheFile the scratch file that live-tuning autosaves into
   * @param saveTableName NetworkTables table to publish the Save button under
   * @param saveEntryName entry name within that table for the Save button
   */
  public ConfigManager(File configFile, File cacheFile) {
    this(
        configFile,
        cacheFile,
        ConfigSaveButton.kDefaultTableName,
        ConfigSaveButton.kDefaultEntryName);
  }

  public ConfigManager(
      File configFile, File cacheFile, String saveTableName, String saveEntryName) {
    this.configFile = configFile;
    this.cacheFile = cacheFile;
    this.document = TypedConfigLoader.load(configFile);

    TypedNetworkTableSync.publish(document);
    this.configListeners =
        TypedNetworkTableSync.listen(
            document,
            () -> {
              if (!DriverStation.isFMSAttached()) {
                TypedConfigSaver.save(cacheFile, document);
                System.out.println("Saved config cache: " + cacheFile.getAbsolutePath());
              }
            });
    this.saveButtonListener =
        ConfigSaveButton.listen(saveTableName, saveEntryName, configFile, document);
    System.out.println("Published typed config to NetworkTables under /Config");
  }

  /** Returns the live typed configuration document. */
  public ConfigDocument getDocument() {
    return document;
  }

  /** Returns the committed config file (loaded at startup, written on Save). */
  public File getConfigFile() {
    return configFile;
  }

  /** Returns the scratch cache file that live-tuning autosaves into. */
  public File getCacheFile() {
    return cacheFile;
  }

  /** Closes the NetworkTable listeners. Call from a robot shutdown hook if one is used. */
  public void close() {
    for (NetworkTableListener listener : configListeners) {
      listener.close();
    }
    saveButtonListener.close();
  }
}
