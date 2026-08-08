package org.team5459.config;

/**
 * Momentary NetworkTables Save control that promotes the live debug document into {@code
 * robot-config.json}.
 *
 * <p>Uses a dashboard-owned pulse ({@link ConfigDashboardPulse}) so Elastic Toggle Button can
 * publish {@code /Config/Save}. Robot-first {@code setBoolean} ownership blocks Elastic publishes.
 */
public final class ConfigSaveButton implements AutoCloseable {
  public static final String kDefaultTableName = "Config";
  public static final String kDefaultEntryName = "Save";

  private final Runnable onSave;
  private final ConfigDashboardPulse pulse;

  public ConfigSaveButton(String tableName, String entryName, Runnable onSave) {
    this.onSave = onSave;
    this.pulse = new ConfigDashboardPulse("/" + tableName + "/" + entryName, "Save");
  }

  ConfigDashboardPulse pulse() {
    return pulse;
  }

  /** No-op retained for call sites that previously forced Save=false. */
  public void publish() {}

  /**
   * Detects a dashboard Save press. Call from robot periodic.
   *
   * @return {@code true} if a press was handled this call
   */
  public boolean poll() {
    if (!pulse.pollRisingEdge()) {
      return false;
    }
    System.out.println("[Config] Save pressed — promoting to robot-config.json");
    if (onSave != null) {
      onSave.run();
    }
    return true;
  }

  @Override
  public void close() {
    pulse.close();
  }
}
