package org.team5459.config;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import edu.wpi.first.networktables.NetworkTableType;
import java.util.EnumSet;

/**
 * Elastic Create panel under {@code /Config/Create}: type chooser, parent folder chooser, name, and
 * Go toggle.
 *
 * <p>Go is handled on {@code kValueRemote} only (Elastic Toggle Button). The robot clearing Go to
 * {@code false} is local and must not re-trigger create.
 *
 * <p>Name is owned by Elastic (Text Display). The robot only reads it and clears it after a
 * successful create.
 */
public final class ConfigCreatePanel implements AutoCloseable {

  public static final String TABLE = "Config";
  public static final String SUBTABLE = "Create";
  public static final String TYPE_SUBTABLE = "Type";
  public static final String FOLDER_SUBTABLE = "Folder";
  public static final String NAME_ENTRY = "Name";
  public static final String GO_ENTRY = "Go";
  private static final String LEGACY_GO_PULSE_ENTRY = "GoPulse";

  private final ConfigDocument document;
  private final Runnable onCreated;
  private final NetworkTableListener goListener;

  public ConfigCreatePanel(ConfigDocument document, Runnable onCreated) {
    this.document = document;
    this.onCreated = onCreated;
    publish();
    NetworkTableEntry goEntry = createTable().getEntry(GO_ENTRY);
    this.goListener =
        NetworkTableListener.createListener(
            goEntry,
            EnumSet.of(NetworkTableEvent.Kind.kValueRemote),
            event -> {
              if (event.valueData == null
                  || event.valueData.value == null
                  || !isTruthy(event.valueData.value)) {
                return;
              }
              goEntry.setBoolean(false);
              handleGo();
            });
  }

  /** Publishes type/folder choosers and Go=false. Does not overwrite Name. */
  public void publish() {
    NetworkTable create = createTable();
    publishStringChooser(
        create.getSubTable(TYPE_SUBTABLE),
        ConfigCreateHelper.typeChooserOptions(),
        ConfigCreateHelper.DEFAULT_TYPE);
    refreshFolderOptions();

    NetworkTableEntry name = create.getEntry(NAME_ENTRY);
    if (!name.exists()) {
      name.setString("");
    }

    NetworkTableEntry legacyPulse = create.getEntry(LEGACY_GO_PULSE_ENTRY);
    if (legacyPulse.exists()) {
      legacyPulse.unpublish();
    }

    create.getEntry(GO_ENTRY).setBoolean(false);
  }

  /** Rebuilds Folder chooser options from the current document. */
  public void refreshFolderOptions() {
    NetworkTable folderTable = createTable().getSubTable(FOLDER_SUBTABLE);
    String[] options = ConfigCreateHelper.folderChooserOptions(document);
    String preferred = folderTable.getEntry("selected").getString("");
    if (preferred == null || preferred.isBlank() || !contains(options, preferred)) {
      preferred = ConfigCreateHelper.ROOT_FOLDER;
    }
    publishStringChooser(folderTable, options, preferred);
  }

  /** Acknowledges chooser selections. Go is handled by the remote listener. */
  public void poll() {
    acknowledgeChooser(createTable().getSubTable(TYPE_SUBTABLE));
    acknowledgeChooser(createTable().getSubTable(FOLDER_SUBTABLE));
  }

  static boolean isGoPulsed(NetworkTableEntry goEntry) {
    NetworkTableType type = goEntry.getType();
    if (type == NetworkTableType.kBoolean) {
      return goEntry.getBoolean(false);
    }
    if (type == NetworkTableType.kDouble || type == NetworkTableType.kFloat) {
      return Math.abs(goEntry.getDouble(0.0)) > 1e-9;
    }
    if (type == NetworkTableType.kInteger) {
      return goEntry.getInteger(0) != 0;
    }
    return goEntry.getBoolean(false);
  }

  private static boolean isTruthy(edu.wpi.first.networktables.NetworkTableValue value) {
    return switch (value.getType()) {
      case kBoolean -> value.getBoolean();
      case kDouble, kFloat -> Math.abs(value.getDouble()) > 1e-9;
      case kInteger -> value.getInteger() != 0;
      default -> false;
    };
  }

  private void handleGo() {
    NetworkTable create = createTable();
    String type =
        readChooserSelection(create.getSubTable(TYPE_SUBTABLE), ConfigCreateHelper.DEFAULT_TYPE);
    String folder =
        readChooserSelection(create.getSubTable(FOLDER_SUBTABLE), ConfigCreateHelper.ROOT_FOLDER);
    String name = create.getEntry(NAME_ENTRY).getString("");
    String path = ConfigCreateHelper.buildPath(folder, name);
    if (path == null) {
      ConfigWarnings.warn(
          "Create ignored: publish a Name first (Enter or submit), folder='"
              + folder
              + "', name='"
              + name
              + "'");
      return;
    }

    System.out.println(
        "[Config] Create requested type="
            + type
            + " folder="
            + folder
            + " name="
            + name
            + " -> "
            + path);
    if (ConfigCreateHelper.create(document, type, path)) {
      clearForm();
      refreshFolderOptions();
      if (onCreated != null) {
        onCreated.run();
      }
    }
  }

  public void clearForm() {
    NetworkTable create = createTable();
    create.getEntry(NAME_ENTRY).setString("");
    create.getEntry(GO_ENTRY).setBoolean(false);
  }

  private static void acknowledgeChooser(NetworkTable table) {
    String selected = table.getEntry("selected").getString("");
    if (selected == null || selected.isBlank()) {
      return;
    }
    String active = table.getEntry("active").getString("");
    if (!selected.equals(active)) {
      table.getEntry("active").setString(selected);
    }
  }

  private static void publishStringChooser(NetworkTable table, String[] options, String preferred) {
    table.getEntry(".type").setString("String Chooser");
    table.getEntry("options").setStringArray(options);
    String defaultOpt = options.length > 0 ? options[0] : "";
    table.getEntry("default").setString(defaultOpt);

    String selected = table.getEntry("selected").getString("");
    if (selected == null || selected.isBlank() || !contains(options, selected)) {
      selected = preferred != null && contains(options, preferred) ? preferred : defaultOpt;
      table.getEntry("selected").setString(selected);
    }
    String active = table.getEntry("active").getString("");
    if (!selected.equals(active)) {
      table.getEntry("active").setString(selected);
    }
  }

  private static String readChooserSelection(NetworkTable table, String fallback) {
    String selected = table.getEntry("selected").getString("");
    if (selected != null && !selected.isBlank()) {
      return selected;
    }
    String active = table.getEntry("active").getString("");
    if (active != null && !active.isBlank()) {
      return active;
    }
    return fallback;
  }

  private static boolean contains(String[] options, String value) {
    if (value == null) {
      return false;
    }
    for (String option : options) {
      if (value.equals(option)) {
        return true;
      }
    }
    return false;
  }

  private static NetworkTable createTable() {
    return NetworkTableInstance.getDefault().getTable(TABLE).getSubTable(SUBTABLE);
  }

  @Override
  public void close() {
    goListener.close();
    createTable().getEntry(GO_ENTRY).setBoolean(false);
  }
}
