package org.team5459.config;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import java.util.EnumSet;

/**
 * Elastic Create panel under {@code /Config/Create}: type ComboBox (String Chooser), path Text
 * Display, and Go toggle. On success, clears Path and sets Go false (type is left as last selected;
 * Elastic's chooser UI typically does not follow robot resets).
 */
public final class ConfigCreatePanel implements AutoCloseable {

  public static final String TABLE = "Config";
  public static final String SUBTABLE = "Create";
  public static final String TYPE_SUBTABLE = "Type";
  public static final String PATH_ENTRY = "Path";
  public static final String GO_ENTRY = "Go";

  private final ConfigDocument document;
  private final Runnable onCreated;
  private final NetworkTableListener goListener;

  public ConfigCreatePanel(ConfigDocument document, Runnable onCreated) {
    this.document = document;
    this.onCreated = onCreated;
    publish();
    var goEntry = createTable().getEntry(GO_ENTRY);
    this.goListener =
        NetworkTableListener.createListener(
            goEntry,
            EnumSet.of(NetworkTableEvent.Kind.kValueRemote, NetworkTableEvent.Kind.kValueLocal),
            event -> {
              if (event.valueData == null
                  || event.valueData.value == null
                  || !event.valueData.value.getBoolean()) {
                return;
              }
              handleGo();
            });
  }

  /** Ensures chooser options, empty Path, and Go=false are published. */
  public void publish() {
    NetworkTable create = createTable();
    NetworkTable typeTable = create.getSubTable(TYPE_SUBTABLE);
    String[] options = ConfigCreateHelper.TYPE_TO_TEMPLATE.keySet().toArray(String[]::new);
    typeTable.getEntry(".type").setString("String Chooser");
    typeTable.getEntry("options").setStringArray(options);
    typeTable.getEntry("default").setString(ConfigCreateHelper.DEFAULT_TYPE);
    String selected = typeTable.getEntry("selected").getString("");
    if (selected == null || selected.isBlank()) {
      selected = ConfigCreateHelper.DEFAULT_TYPE;
    }
    typeTable.getEntry("selected").setString(selected);
    typeTable.getEntry("active").setString(selected);
    create.getEntry(PATH_ENTRY).setString(create.getEntry(PATH_ENTRY).getString(""));
    create.getEntry(GO_ENTRY).setBoolean(false);
  }

  private void handleGo() {
    NetworkTable create = createTable();
    String path = create.getEntry(PATH_ENTRY).getString("");
    String type = create.getSubTable(TYPE_SUBTABLE).getEntry("selected").getString("");
    if (type == null || type.isBlank()) {
      type =
          create
              .getSubTable(TYPE_SUBTABLE)
              .getEntry("active")
              .getString(ConfigCreateHelper.DEFAULT_TYPE);
    }

    boolean created = ConfigCreateHelper.create(document, type, path);
    create.getEntry(GO_ENTRY).setBoolean(false);
    if (created) {
      clearForm();
      if (onCreated != null) {
        onCreated.run();
      }
    }
  }

  /**
   * Clears Path and Go. Type stays as last selected: Elastic's ComboBox Chooser keeps its own UI
   * selection and often ignores robot writes to {@code selected}/{@code active}.
   */
  public void clearForm() {
    NetworkTable create = createTable();
    create.getEntry(PATH_ENTRY).setString("");
    create.getEntry(GO_ENTRY).setBoolean(false);
  }

  private static NetworkTable createTable() {
    return NetworkTableInstance.getDefault().getTable(TABLE).getSubTable(SUBTABLE);
  }

  @Override
  public void close() {
    goListener.close();
  }
}
