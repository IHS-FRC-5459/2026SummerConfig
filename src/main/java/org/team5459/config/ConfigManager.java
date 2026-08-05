package org.team5459.config;

import edu.wpi.first.networktables.NetworkTableListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Owns the lifecycle of deploy-backed typed configuration.
 *
 * <h2>Modes</h2>
 *
 * <ul>
 *   <li><b>Debug</b> ({@code !FMS} and {@code Config/DebugMode == true}): NetworkTables edits
 *       update the live document; getters read that NT-backed document; edits autosave to {@code
 *       config-cache.json}. Promote to {@code robot-config.json} via the Save widget or when a
 *       watched file (default {@code elastic-layout.json}) changes content (Elastic Save As).
 *   <li><b>Match</b> (FMS attached, or DebugMode widget false): publish defaults to NT for display
 *       only; ignore NT writebacks; do not write JSON; getters read the JSON defaults loaded at
 *       startup.
 * </ul>
 *
 * <p>Typical usage from {@code Robot.robotInit()} / {@code robotPeriodic()}:
 *
 * <pre>{@code
 * configManager = new ConfigManager(
 *     new File(Filesystem.getDeployDirectory(), "robot-config.json"),
 *     new File(Filesystem.getDeployDirectory(), "config-cache.json"));
 * // each loop:
 * configManager.periodic();
 * }</pre>
 */
public class ConfigManager {
  private final File configFile;
  private final File cacheFile;
  private final File watchFile;
  private final ConfigDocument defaultsDocument;
  private final ConfigDocument liveDocument;
  private final ConfigDebugMode debugMode;
  private final ConfigPromoteWatcher promoteWatcher;

  private final List<NetworkTableListener> configListeners = new ArrayList<>();
  private NetworkTableListener saveButtonListener;
  private boolean debugActive;
  private boolean promoting;

  /**
   * Loads {@code configFile}, publishes to NetworkTables, and enables debug tooling when
   * appropriate. Watches a sibling {@code elastic-layout.json} for Save As promote triggers.
   */
  public ConfigManager(File configFile, File cacheFile) {
    this(configFile, cacheFile, sibling(configFile, "elastic-layout.json"));
  }

  public ConfigManager(File configFile, File cacheFile, File watchFile) {
    this(
        configFile,
        cacheFile,
        watchFile,
        ConfigSaveButton.kDefaultTableName,
        ConfigSaveButton.kDefaultEntryName);
  }

  public ConfigManager(
      File configFile, File cacheFile, File watchFile, String saveTableName, String saveEntryName) {
    this.configFile = configFile;
    this.cacheFile = cacheFile;
    this.watchFile = watchFile;
    this.defaultsDocument = TypedConfigLoader.load(configFile);
    this.liveDocument = TypedConfigLoader.load(configFile);
    this.debugMode = new ConfigDebugMode();
    this.promoteWatcher = new ConfigPromoteWatcher(watchFile, this::promoteFromFileWatch);
    // Baseline the watched file now so the first real Save As rewrite promotes.
    this.promoteWatcher.poll();

    TypedNetworkTableSync.publish(defaultsDocument);
    ConfigSaveButton.publish(saveTableName, saveEntryName);
    this.saveButtonListener =
        ConfigSaveButton.listen(saveTableName, saveEntryName, this::promoteFromSaveButton);

    if (debugMode.isDebug()) {
      enableDebug();
    } else {
      debugActive = false;
      System.out.println(
          "[Config] Match mode: getters use robot-config.json defaults; NT writebacks ignored");
    }
  }

  /**
   * Re-evaluates debug vs match mode and polls the Save As file watcher. Call from {@code
   * robotPeriodic()}.
   */
  public void periodic() {
    boolean wantDebug = debugMode.isDebug();
    if (wantDebug != debugActive) {
      if (wantDebug) {
        enableDebug();
      } else {
        disableDebug();
      }
    }
    if (debugActive) {
      promoteWatcher.poll();
    }
  }

  /**
   * Document used by robot code getters.
   *
   * <p>Debug: live NT-synced document. Match: immutable-at-runtime JSON defaults from {@code
   * robot-config.json}.
   */
  public ConfigDocument getDocument() {
    return debugActive ? liveDocument : defaultsDocument;
  }

  /** Whether debug (live-tuning) mode is currently active. */
  public boolean isDebugMode() {
    return debugActive;
  }

  public File getConfigFile() {
    return configFile;
  }

  public File getCacheFile() {
    return cacheFile;
  }

  public File getWatchFile() {
    return watchFile;
  }

  /** Writes the live document to {@code robot-config.json} when in debug mode. */
  public void promote() {
    if (!debugActive) {
      System.out.println("[Config] Promote ignored: not in debug mode");
      return;
    }
    if (promoting) {
      return;
    }
    promoting = true;
    try {
      // Live document is already kept in sync by NT listeners in debug mode.
      TypedConfigSaver.save(configFile, liveDocument);
      TypedConfigOverlay.apply(liveDocument, defaultsDocument);
      System.out.println("[Config] Promoted live values to " + configFile.getAbsolutePath());
    } finally {
      promoting = false;
    }
  }

  public void close() {
    disableDebugListenersOnly();
    if (saveButtonListener != null) {
      saveButtonListener.close();
      saveButtonListener = null;
    }
  }

  private void enableDebug() {
    TypedConfigOverlay.apply(defaultsDocument, liveDocument);
    TypedConfigOverlay.applyFile(cacheFile, liveDocument);
    TypedNetworkTableSync.publish(liveDocument);
    TypedNetworkTablePull.pull(liveDocument);
    startConfigListeners();
    debugActive = true;
    System.out.println(
        "[Config] Debug mode: getters use NetworkTables; autosaving " + cacheFile.getName());
  }

  private void disableDebug() {
    disableDebugListenersOnly();
    TypedNetworkTableSync.publish(defaultsDocument);
    debugActive = false;
    System.out.println(
        "[Config] Match mode: getters use robot-config.json defaults; NT writebacks ignored");
  }

  private void startConfigListeners() {
    disableDebugListenersOnly();
    NetworkTableListener[] listeners =
        TypedNetworkTableSync.listen(
            liveDocument,
            () -> {
              if (!debugActive) {
                return;
              }
              TypedConfigSaver.save(cacheFile, liveDocument);
              System.out.println("[Config] Saved config cache: " + cacheFile.getAbsolutePath());
            });
    for (NetworkTableListener listener : listeners) {
      configListeners.add(listener);
    }
  }

  private void disableDebugListenersOnly() {
    for (NetworkTableListener listener : configListeners) {
      listener.close();
    }
    configListeners.clear();
  }

  private void promoteFromSaveButton() {
    promote();
  }

  private void promoteFromFileWatch() {
    System.out.println(
        "[Config] Watched file changed ("
            + (watchFile == null ? "?" : watchFile.getName())
            + "); promoting");
    promote();
  }

  private static File sibling(File file, String name) {
    File parent = file.getParentFile();
    return parent == null ? new File(name) : new File(parent, name);
  }
}
