package org.team5459.config;

import edu.wpi.first.networktables.NetworkTableListener;
import java.util.ArrayList;
import java.util.List;
import org.team5459.config.entries.ConfigEntry;

/** Publishes a {@link ConfigDocument} to NetworkTables and listens for remote edits. */
public final class NetworkTableSync {

  private NetworkTableSync() {}

  public static void publish(ConfigDocument document) {
    for (ConfigEntry entry : document.getEntries().values()) {
      entry.publish();
    }
  }

  public static NetworkTableListener[] listen(ConfigDocument document) {
    return listen(document, () -> {});
  }

  public static NetworkTableListener[] listen(ConfigDocument document, Runnable onUpdate) {
    List<NetworkTableListener> listeners = new ArrayList<>();
    for (ConfigEntry entry : document.getEntries().values()) {
      listeners.addAll(entry.listen(onUpdate));
    }
    return listeners.toArray(NetworkTableListener[]::new);
  }
}
