package org.team5459.config;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import java.util.EnumSet;

/**
 * Synchronizes configuration values with NetworkTables.
 *
 * <p>This class provides two complementary operations:
 *
 * <ul>
 *   <li>{@link #publish(Object)} publishes the current configuration values to NetworkTables so
 *       they can be viewed or modified externally (for example, from Elastic or another dashboard).
 *   <li>{@link #listen(Object)} and {@link #listen(Object, Runnable)} create listeners that
 *       automatically update the configuration object whenever a remote NetworkTables client
 *       changes a value.
 * </ul>
 *
 * <p>Each public instance field of the configuration object is mapped to a NetworkTables entry with
 * the same name under the "Config" table.
 *
 * <p>Currently, only {@code int}/{@code Integer} and {@code double}/{@code Double} fields are
 * supported.
 */
public final class NetworkTableSync {

  /** Utility class; should never be instantiated. */
  private NetworkTableSync() {}

  /**
   * Publishes every public configuration field to NetworkTables.
   *
   * <p>The field name becomes the NetworkTables key and the current field value becomes the entry
   * value. This is typically called once during robot startup after loading the configuration from
   * disk.
   *
   * @param config Configuration object to publish
   * @throws IllegalArgumentException if an unsupported field type is encountered
   */
  public static void publish(Object config) {
    NetworkTable table = NetworkTableInstance.getDefault().getTable("Config");

    for (var field : ReflectionWalker.getPublicInstanceFields(config.getClass())) {
      Object value = ReflectionWalker.getValue(field, config);

      if (value instanceof Double doubleValue) {
        table.getEntry(field.getName()).setDouble(doubleValue);

      } else if (value instanceof Integer integerValue) {
        table.getEntry(field.getName()).setInteger(integerValue.longValue());

      } else {
        throw new IllegalArgumentException(
            "Unsupported config field type for " + field.getName() + ": " + field.getType());
      }
    }
  }

  /**
   * Creates listeners that update the configuration object whenever a remote NetworkTables client
   * changes a value.
   *
   * <p>No callback is executed after updates are applied.
   *
   * @param config Configuration object to keep synchronized
   * @return Array of listeners that must be retained to keep them alive
   */
  public static NetworkTableListener[] listen(Object config) {
    return listen(config, () -> {});
  }

  /**
   * Creates listeners that update the configuration object whenever a remote NetworkTables client
   * changes a value.
   *
   * <p>Each public configuration field receives its own listener. Whenever a supported value
   * changes remotely, the corresponding field is updated via reflection and the supplied callback
   * is invoked.
   *
   * <p>Only {@link NetworkTableEvent.Kind#kValueRemote} events are observed. This prevents updates
   * originating from the robot itself from immediately triggering another update cycle.
   *
   * @param config Configuration object to synchronize
   * @param onUpdate Callback executed after every successful update
   * @return Array of listeners that must be retained to keep them alive
   * @throws IllegalArgumentException if an unsupported field type is encountered
   */
  public static NetworkTableListener[] listen(Object config, Runnable onUpdate) {
    NetworkTable table = NetworkTableInstance.getDefault().getTable("Config");

    return java.util.Arrays.stream(ReflectionWalker.getPublicInstanceFields(config.getClass()))
        .map(
            field -> {
              NetworkTableEntry entry = table.getEntry(field.getName());

              return NetworkTableListener.createListener(
                  entry,
                  // Only respond to values changed by remote clients (Elastic,
                  // Glass, Shuffleboard, etc.).
                  EnumSet.of(NetworkTableEvent.Kind.kValueRemote),
                  event -> {
                    // Ignore malformed events that do not contain a value.
                    if (event.valueData == null) {
                      return;
                    }

                    if (field.getType() == double.class || field.getType() == Double.class) {
                      ReflectionWalker.setValue(field, config, event.valueData.value.getDouble());

                    } else if (field.getType() == int.class || field.getType() == Integer.class) {
                      ReflectionWalker.setValue(
                          field, config, (int) event.valueData.value.getInteger());

                    } else {
                      throw new IllegalArgumentException(
                          "Unsupported config field type for "
                              + field.getName()
                              + ": "
                              + field.getType());
                    }

                    // Helpful for debugging configuration updates during
                    // development.
                    System.out.println(
                        field.getName() + " = " + ReflectionWalker.getValue(field, config));

                    // Notify the caller that the configuration has changed.
                    onUpdate.run();
                  });
            })
        .toArray(NetworkTableListener[]::new);
  }
}
