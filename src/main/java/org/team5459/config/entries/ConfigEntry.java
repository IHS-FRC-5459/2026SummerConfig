package org.team5459.config.entries;

import edu.wpi.first.networktables.NetworkTableListener;
import java.util.List;

/** A runtime config entry backed by one Elastic container. */
public interface ConfigEntry {

  /** Canonical absolute NetworkTables topic for this entry. */
  String topic();

  /** Elastic widget {@code type} string. */
  String widgetType();

  /** Publishes current values to NetworkTables. */
  void publish();

  /** Attaches remote-edit listeners; returns listeners to close on shutdown. */
  List<NetworkTableListener> listen(Runnable onUpdate);
}
