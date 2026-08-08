package org.team5459.config;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import java.util.EnumSet;

/**
 * Elastic Delete panel under {@code /Config/Delete}: path chooser + Go toggle (debug only).
 *
 * <p>Go is handled on {@code kValueRemote} only (Elastic Toggle Button). Selecting {@link
 * ConfigDeleteHelper#NONE_OPTION} is a no-op.
 */
public final class ConfigDeletePanel implements AutoCloseable {

  public static final String TABLE = "Config";
  public static final String SUBTABLE = "Delete";
  public static final String PATH_SUBTABLE = "Path";
  public static final String GO_ENTRY = "Go";

  private final ConfigDocument document;
  private final Runnable onDeleted;
  private final NetworkTableListener goListener;

  public ConfigDeletePanel(ConfigDocument document, Runnable onDeleted) {
    this.document = document;
    this.onDeleted = onDeleted;
    publish();
    NetworkTableEntry goEntry = deleteTable().getEntry(GO_ENTRY);
    this.goListener =
        NetworkTableListener.createListener(
            goEntry,
            EnumSet.of(NetworkTableEvent.Kind.kValueRemote),
            event -> {
              if (event.valueData == null
                  || event.valueData.value == null
                  || !event.valueData.value.getBoolean()) {
                return;
              }
              goEntry.setBoolean(false);
              handleGo();
            });
  }

  /** Publishes path chooser and Go=false. */
  public void publish() {
    refreshPathOptions();
    deleteTable().getEntry(GO_ENTRY).setBoolean(false);
    System.out.println(
        "[Config] *** Reload Elastic Delete tab from src/main/deploy/elastic-layout.json"
            + " (dashboard does NOT auto-update). ***");
  }

  /** Rebuilds Path chooser options from the current document. */
  public void refreshPathOptions() {
    NetworkTable pathTable = deleteTable().getSubTable(PATH_SUBTABLE);
    String[] options = ConfigDeleteHelper.pathChooserOptions(document);
    String preferred = pathTable.getEntry("selected").getString("");
    if (preferred == null || preferred.isBlank() || !contains(options, preferred)) {
      preferred = ConfigDeleteHelper.NONE_OPTION;
    }
    publishStringChooser(pathTable, options, preferred);
  }

  /** Acknowledges path chooser selection. Go is handled by the remote listener. */
  public void poll() {
    acknowledgeChooser(deleteTable().getSubTable(PATH_SUBTABLE));
  }

  private void handleGo() {
    NetworkTable delete = deleteTable();
    String path =
        readChooserSelection(delete.getSubTable(PATH_SUBTABLE), ConfigDeleteHelper.NONE_OPTION);
    System.out.println("[Config] Delete requested path='" + path + "'");
    boolean deleted = ConfigDeleteHelper.delete(document, path);
    if (deleted) {
      refreshPathOptions();
      if (onDeleted != null) {
        onDeleted.run();
      }
    }
    delete.getEntry(GO_ENTRY).setBoolean(false);
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
    String defaultOpt = options.length > 0 ? options[0] : ConfigDeleteHelper.NONE_OPTION;
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

  private static NetworkTable deleteTable() {
    return NetworkTableInstance.getDefault().getTable(TABLE).getSubTable(SUBTABLE);
  }

  @Override
  public void close() {
    goListener.close();
    deleteTable().getEntry(GO_ENTRY).setBoolean(false);
  }
}
