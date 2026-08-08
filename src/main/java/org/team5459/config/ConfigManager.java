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
  private boolean pendingStructureChange;
  private long lastButtonHeartbeatMs;
  private boolean loggedPeriodicStart;

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
    System.out.println(
        "[Config] Manager ready. Watch robot console for [Config][Pulse] / [Config][Buttons]"
            + " while clicking Save / Create Go / Delete Go.");
  }

  public void periodic() {
    if (!loggedPeriodicStart) {
      loggedPeriodicStart = true;
      System.out.println(
          "[Config] periodic() running (configManager is live). debugActive=" + debugActive);
    }

    if (saveButton != null) {
      saveButton.poll();
    }

    boolean wantDebug = debugMode.isDebug();
    if (wantDebug != debugActive) {
      System.out.println(
          "[Config] DebugMode transition wantDebug=" + wantDebug + " was=" + debugActive);
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
      if (pendingStructureChange) {
        pendingStructureChange = false;
        System.out.println("[Config] Applying deferred Create/Delete structure refresh");
        applyDocumentStructureChange();
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

    logButtonHeartbeat();
  }

  /** Every 2s: dump Save / Create Go / Delete Go NT visibility for Elastic debugging. */
  private void logButtonHeartbeat() {
    long now = System.currentTimeMillis();
    if (now - lastButtonHeartbeatMs < 2000L) {
      return;
    }
    lastButtonHeartbeatMs = now;

    String saveStatus = saveButton != null ? saveButton.pulse().statusLine() : "Save{MISSING}";
    String createStatus =
        createPanel != null ? createPanel.goPulse().statusLine() : "Create/Go{panel=null}";
    String deleteStatus =
        deletePanel != null ? deletePanel.goPulse().statusLine() : "Delete/Go{panel=null}";

    var inst = NetworkTableInstance.getDefault();
    boolean rawSave = inst.getTable("Config").getEntry("Save").getBoolean(false);
    boolean rawCreate =
        inst.getTable("Config").getSubTable("Create").getEntry("Go").getBoolean(false);
    boolean rawDelete =
        inst.getTable("Config").getSubTable("Delete").getEntry("Go").getBoolean(false);
    boolean rawDebug = inst.getTable("Config").getEntry("DebugMode").getBoolean(true);

    System.out.println(
        "[Config][Buttons] debugActive="
            + debugActive
            + " DebugMode(raw)="
            + rawDebug
            + " | "
            + saveStatus
            + " rawSave="
            + rawSave
            + " | "
            + createStatus
            + " rawCreateGo="
            + rawCreate
            + " | "
            + deleteStatus
            + " rawDeleteGo="
            + rawDelete);
  }

  private static void clearGoIfPulsed(String table, String sub, String goEntry, String label) {
    var entry =
        NetworkTableInstance.getDefault().getTable(table).getSubTable(sub).getEntry(goEntry);
    if (entry.getBoolean(false)) {
      entry.setBoolean(false);
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
      System.out.println("[Config] Promote ignored: already promoting");
      return;
    }
    promoting = true;
    try {
      // Do not poll the dynamic registrar here: registration republishes the live document and can
      // overwrite fresher Elastic values on NT before we pull.
      NetworkTableInstance.getDefault().flush();
      logPromoteSample("before-pull");
      TypedNetworkTablePull.pull(liveDocument);
      logPromoteSample("after-pull");

      TypedConfigSaver.save(configFile, liveDocument);
      TypedConfigSaver.save(cacheFile, liveDocument);

      ConfigDocument written = TypedConfigLoader.load(configFile);
      defaultsDocument.replaceRoot(written.getRootEntries());
      System.out.println(
          "[Config][Promote] verified on disk Arm/PIDController/p="
              + written.getDouble("Arm/PIDController/p")
              + " Arm/operatorOffset="
              + written.getDouble("Arm/operatorOffset")
              + " keys="
              + written.getRootEntries().keySet());
      System.out.println("[Config] Promoted live values to " + configFile.getAbsolutePath());
    } finally {
      promoting = false;
    }
  }

  private void logPromoteSample(String phase) {
    double docP = liveDocument.getDouble("Arm/PIDController/p");
    double docOffset = liveDocument.getDouble("Arm/operatorOffset");
    var armPid =
        NetworkTableInstance.getDefault()
            .getTable("Config")
            .getSubTable("Arm")
            .getSubTable("PIDController");
    double ntP = armPid.getEntry("p").getDouble(Double.NaN);
    double ntOffset =
        NetworkTableInstance.getDefault()
            .getTable("Config")
            .getSubTable("Arm")
            .getEntry("operatorOffset")
            .getDouble(Double.NaN);
    System.out.println(
        "[Config][Promote] "
            + phase
            + " doc p="
            + docP
            + " nt p="
            + ntP
            + " doc offset="
            + docOffset
            + " nt offset="
            + ntOffset
            + " rootKeys="
            + liveDocument.getRootEntries().keySet());
  }

  public void close() {
    disableDebugListenersOnly();
    if (saveButton != null) {
      saveButton.close();
      saveButton = null;
    }
  }

  private void enableDebug() {
    debugMode.ensureTypedBoolean(true);
    TypedConfigOverlay.apply(defaultsDocument, liveDocument);
    TypedConfigOverlay.applyFile(cacheFile, liveDocument);
    TypedNetworkTableSync.publish(liveDocument);
    TypedNetworkTablePull.pull(liveDocument);
    debugActive = true;
    if (saveButton != null) {
      saveButton.ensurePublished();
    }
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
    pendingStructureChange = false;
    System.out.println(
        "[Config] Match mode: getters use robot-config.json defaults; NT writebacks ignored");
  }

  private void startConfigListeners() {
    closeConfigValueListeners();
    // Initial setDouble/setBoolean while attaching listeners fires kValueLocal; ignore those so we
    // do not recurse into autosave during Create/Delete structure updates.
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
    // Defer until after Create/Delete Go handling finishes so we do not close the pulse mid-press.
    System.out.println("[Config] Structure change requested — deferring until end of periodic");
    pendingStructureChange = true;
  }

  private void applyDocumentStructureChange() {
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
    if (saveButton != null) {
      saveButton.ensurePublished();
    }
    autosaveCache();
  }

  private void autosaveCache() {
    if (!debugActive || suppressAutosave || autosaving) {
      return;
    }
    autosaving = true;
    try {
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
