package org.team5459.config.entries;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import java.util.EnumSet;
import java.util.List;
import org.team5459.config.TopicPath;

/** Scalar double tunable from a Text Display (or similar) widget. */
public final class DoubleEntry implements ConfigEntry {

  private static final EnumSet<NetworkTableEvent.Kind> REMOTE_VALUE_EVENTS =
      EnumSet.of(NetworkTableEvent.Kind.kValueRemote);

  private final String topic;
  private final String widgetType;
  private double value;

  public DoubleEntry(String topic, String widgetType, double value) {
    this.topic = TopicPath.normalize(topic);
    this.widgetType = widgetType;
    this.value = value;
  }

  @Override
  public String topic() {
    return topic;
  }

  @Override
  public String widgetType() {
    return widgetType;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

  @Override
  public void publish() {
    entry().setDouble(value);
  }

  @Override
  public List<NetworkTableListener> listen(Runnable onUpdate) {
    NetworkTableEntry ntEntry = entry();
    ntEntry.setDouble(value);
    return List.of(
        NetworkTableListener.createListener(
            ntEntry,
            REMOTE_VALUE_EVENTS,
            event -> {
              if (event.valueData == null) {
                return;
              }
              value = event.valueData.value.getDouble();
              onUpdate.run();
            }));
  }

  private NetworkTableEntry entry() {
    TopicPath.TableEntry parts = TopicPath.split(topic);
    return NetworkTableInstance.getDefault()
        .getTable(parts.tablePath())
        .getEntry(parts.entryName());
  }
}
