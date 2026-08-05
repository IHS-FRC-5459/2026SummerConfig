package org.team5459.config;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * Determines whether config runs in debug (live NetworkTables tuning) or match mode.
 *
 * <p>Debug is active when the robot is <strong>not</strong> FMS-attached <strong>and</strong> the
 * {@code Config/DebugMode} dashboard boolean is {@code true} (defaults to {@code true}). On an FMS
 * field connection, match mode is forced regardless of the widget.
 */
public final class ConfigDebugMode {
  public static final String kDefaultTableName = "Config";
  public static final String kDefaultEntryName = "DebugMode";

  private final NetworkTableEntry debugEntry;

  public ConfigDebugMode() {
    this(kDefaultTableName, kDefaultEntryName);
  }

  public ConfigDebugMode(String tableName, String entryName) {
    this.debugEntry = NetworkTableInstance.getDefault().getTable(tableName).getEntry(entryName);
    if (!debugEntry.exists()) {
      debugEntry.setBoolean(true);
    }
  }

  /**
   * Returns whether live tuning is enabled.
   *
   * <p>Match / FMS: always {@code false}. Otherwise follows the {@code DebugMode} widget.
   */
  public boolean isDebug() {
    boolean fmsAttached = DriverStation.isFMSAttached();
    boolean widget = debugEntry.getBoolean(true);
    if (fmsAttached) {
      return false;
    }
    return widget;
  }

  NetworkTableEntry getEntry() {
    return debugEntry;
  }
}
