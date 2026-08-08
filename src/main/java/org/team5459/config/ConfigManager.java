package org.team5459.config;

import edu.wpi.first.networktables.NetworkTableInstance;
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
 *       watched file (default {@code elastic-layout.json}) changes content (Elastic Save As). New
 *       scalar topics under {@code /Config} are auto-registered (Elastic Custom + rebind).
 *   <li><b>Match</b> (FMS attached, or DebugMode widget false): publish defaults to NT for display
 *       only; ignore NT writebacks; do not write JSON; getters read the JSON defaults loaded at
 *       startup.
 * </ul>
 *
 * <h2>Creating / deleting constants</h2>
 *
 * <ul>
 *   <li>Edit {@code robot-config.json} (and deploy).
 *   <li>Debug Create panel: type + parent folder + name under {@code /Config/Create}, then Go.
 *   <li>Debug Delete panel: path under {@code /Config/Delete}, then Go (folders remove children).
 *   <li>Elastic Custom scalar → rebind topic to {@code /Config/...} (debug auto-register).
 * </ul>
 */
public class ConfigManager {
  private final File configFile;
  private final File cacheFile;
  private final File watchFile;
  private final String saveTableName;
  private final String saveEntryName;
  private final ConfigDocument defaultsDocument;
  private final ConfigDocument liveDocument;
  private final ConfigDebugMode debugMode;
  private final ConfigPromoteWatcher promoteWatcher;

  private final List<NetworkTableListener> configListeners = new ArrayList<>();
  private ConfigSaveButton saveButton;
  private ConfigDynamicRegistrar dynamicRegistrar;
  private ConfigCreatePanel createPanel;
  private ConfigDeletePanel deletePanel;
  private boolean debugActive;
  private boolean promoting;
  private boolean autosaving;
  private boolean suppressAutosave;

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
    this.saveTableName = saveTableName;
    this.saveEntryName = saveEntryName;
    this.defaultsDocument = TypedConfigLoader.load(configFile);
    this.liveDocument = TypedConfigLoader.load(configFile);
    this.debugMode = new ConfigDebugMode();
    this.promoteWatcher = new ConfigPromoteWatcher(watchFile, this::promoteFromFileWatch);
    this.promoteWatcher.poll();

    TypedNetworkTableSync.publish(defaultsDocument);
    this.saveButton =
        new ConfigSaveButton(saveTableName, saveEntryName, this::promoteFromSaveButton);

    if (debugMode.isDebug()) {
      enableDebug();
    } else {
      debugActive = false;
      System.out.println(
          "[Config] Match mode: getters use robot-config.json defaults; NT writebacks ignored");
    }
  }

  public void periodic() {
    if (saveButton != null) {
      saveButton.poll();
    }

    boolean wantDebug = debugMode.isDebug();
    if (wantDebug != debugActive) {
      if (wantDebug) {
        enableDebug();
      } else {
        disableDebug();
      }
    }

    if (debugActive) {
      if (createPanel != null) {
        createPanel.poll();
      }
      if (deletePanel != null) {
        deletePanel.poll();
      }
      promoteWatcher.poll();
      if (dynamicRegistrar != null) {
        dynamicRegistrar.poll();
      }
    } else {
      clearGoIfPulsed(
          ConfigCreatePanel.TABLE,
          ConfigCreatePanel.SUBTABLE,
          ConfigCreatePanel.GO_ENTRY,
          "Create");
      clearGoIfPulsed(
          ConfigDeletePanel.TABLE,
          ConfigDeletePanel.SUBTABLE,
          ConfigDeletePanel.GO_ENTRY,
          "Delete");
    }
  }

  private static void clearGoIfPulsed(String table, String sub, String goEntry, String label) {
    var entry =
        NetworkTableInstance.getDefault().getTable(table).getSubTable(sub).getEntry(goEntry);
    if (ConfigCreatePanel.isGoPulsed(entry)) {
      entry.setBoolean(false);
      NetworkTableInstance.getDefault().flush();
      ConfigWarnings.warn(label + " ignored: turn on Config/DebugMode first");
    }
  }

  public ConfigDocument getDocument() {
    return debugActive ? liveDocument : defaultsDocument;
  }

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
      // PIDController Elastic widget only writes NT on "Publish Values"; pull so Save persists
      // those values (and any listener-missed scalars) instead of stale in-memory defaults.
      if (dynamicRegistrar != null) {
        dynamicRegistrar.poll();
      }
      TypedNetworkTablePull.pull(liveDocument);
      TypedConfigSaver.save(configFile, liveDocument);
      defaultsDocument.replaceRoot(TypedConfigLoader.load(configFile).getRootEntries());
      System.out.println("[Config] Promoted live values to " + configFile.getAbsolutePath());
    } finally {
      promoting = false;
    }
  }

  public void close() {
    disableDebugListenersOnly();
    if (saveButton != null) {
      saveButton.close();
      saveButton = null;
    }
  }

  private void enableDebug() {
    TypedConfigOverlay.apply(defaultsDocument, liveDocument);
    TypedConfigOverlay.applyFile(cacheFile, liveDocument);
    TypedNetworkTableSync.publish(liveDocument);
    TypedNetworkTablePull.pull(liveDocument);
    debugActive = true;
    startConfigListeners();
    startDynamicRegistrar();
    startCreatePanel();
    startDeletePanel();
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
    closeConfigValueListeners();
    // Initial setDouble/setBoolean while attaching listeners fires kValueLocal; ignore those so we
    // do not recurse into autosave/pull during Create/Delete structure updates.
    suppressAutosave = true;
    try {
      NetworkTableListener[] listeners =
          TypedNetworkTableSync.listen(liveDocument, this::autosaveCache);
      for (NetworkTableListener listener : listeners) {
        configListeners.add(listener);
      }
    } finally {
      suppressAutosave = false;
    }
  }

  private void startDynamicRegistrar() {
    closeDynamicRegistrar();
    dynamicRegistrar = new ConfigDynamicRegistrar(liveDocument, this::onDocumentStructureChanged);
  }

  private void startCreatePanel() {
    closeCreatePanel();
    createPanel = new ConfigCreatePanel(liveDocument, this::onDocumentStructureChanged);
  }

  private void startDeletePanel() {
    closeDeletePanel();
    deletePanel = new ConfigDeletePanel(liveDocument, this::onDocumentStructureChanged);
    System.out.println(
        "[Config] *** Reload Elastic layout from elastic-layout.json to pick up Create/Delete tabs"
            + " (dashboard does NOT auto-refresh). ***");
  }

  private void onDocumentStructureChanged() {
    if (!debugActive) {
      return;
    }
    TypedNetworkTableSync.publish(liveDocument);
    startConfigListeners();
    if (createPanel != null) {
      createPanel.refreshFolderOptions();
    }
    if (deletePanel != null) {
      deletePanel.refreshPathOptions();
    }
    autosaveCache();
  }

  private void autosaveCache() {
    if (!debugActive || suppressAutosave || autosaving) {
      return;
    }
    autosaving = true;
    try {
      // Do not pull here: Create/Delete treat the document as source of truth, and listener attach
      // floods would pull+write on every setDouble. Save/promote pulls NT before writing
      // robot-config.json.
      TypedConfigSaver.save(cacheFile, liveDocument);
      System.out.println("[Config] Saved config cache: " + cacheFile.getAbsolutePath());
    } finally {
      autosaving = false;
    }
  }

  private void disableDebugListenersOnly() {
    closeConfigValueListeners();
    closeDynamicRegistrar();
    closeCreatePanel();
    closeDeletePanel();
  }

  private void closeConfigValueListeners() {
    for (NetworkTableListener listener : configListeners) {
      listener.close();
    }
    configListeners.clear();
  }

  private void closeDynamicRegistrar() {
    if (dynamicRegistrar != null) {
      dynamicRegistrar.close();
      dynamicRegistrar = null;
    }
  }

  private void closeCreatePanel() {
    if (createPanel != null) {
      createPanel.close();
      createPanel = null;
    }
  }

  private void closeDeletePanel() {
    if (deletePanel != null) {
      deletePanel.close();
      deletePanel = null;
    }
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
