package org.team5459.config;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableType;
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
    // Always publish a typed boolean. A leftover kUnassigned topic (exists but untyped) blocks
    // Elastic Toggle Switch writes the same way Save/Go did.
    ensureTypedBoolean(true);
    System.out.println(
        "[Config][DebugMode] published typed boolean val="
            + debugEntry.getBoolean(true)
            + " exists="
            + debugEntry.exists()
            + " type="
            + debugEntry.getType());
  }

  /** Re-asserts a typed boolean publisher so Elastic can use updateDataFromTopic. */
  void ensureTypedBoolean(boolean defaultIfMissing) {
    NetworkTableType before = debugEntry.getType();
    boolean value =
        before == NetworkTableType.kBoolean
            ? debugEntry.getBoolean(defaultIfMissing)
            : defaultIfMissing;
    debugEntry.setBoolean(value);
    NetworkTableType after = debugEntry.getType();
    if (before != after || after != NetworkTableType.kBoolean) {
      System.out.println(
          "[Config][DebugMode] ensureTypedBoolean before="
              + before
              + " after="
              + after
              + " val="
              + value);
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
