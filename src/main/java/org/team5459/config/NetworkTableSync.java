package org.team5459.config;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import java.util.EnumSet;

public final class NetworkTableSync {

  private NetworkTableSync() {}

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

  public static NetworkTableListener[] listen(Object config) {
    return listen(config, () -> {});
  }

  public static NetworkTableListener[] listen(Object config, Runnable onUpdate) {
    NetworkTable table = NetworkTableInstance.getDefault().getTable("Config");

    return java.util.Arrays.stream(ReflectionWalker.getPublicInstanceFields(config.getClass()))
        .map(
            field -> {
              NetworkTableEntry entry = table.getEntry(field.getName());
              return NetworkTableListener.createListener(
                  entry,
                  EnumSet.of(NetworkTableEvent.Kind.kValueRemote),
                  event -> {
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

                    System.out.println(
                        field.getName() + " = " + ReflectionWalker.getValue(field, config));
                    onUpdate.run();
                  });
            })
        .toArray(NetworkTableListener[]::new);
  }
}
