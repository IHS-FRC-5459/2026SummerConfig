package org.team5459.config.entries;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.team5459.config.TopicPath;

/**
 * Live {@link PIDController} from an Elastic {@code PIDController} widget.
 *
 * <p>Publishes Sendable-shaped NetworkTables fields ({@code .type}, {@code p}, {@code i}, {@code
 * d}, {@code setpoint}) so Elastic can show the native PID card.
 */
public final class PIDControllerEntry implements ConfigEntry {

  private static final EnumSet<NetworkTableEvent.Kind> REMOTE_VALUE_EVENTS =
      EnumSet.of(NetworkTableEvent.Kind.kValueRemote);

  private final String topic;
  private final PIDController controller = new PIDController(0.0, 0.0, 0.0);

  public PIDControllerEntry(String topic, double p, double i, double d, double setpoint) {
    this.topic = TopicPath.normalize(topic);
    controller.setPID(p, i, d);
    controller.setSetpoint(setpoint);
  }

  @Override
  public String topic() {
    return topic;
  }

  @Override
  public String widgetType() {
    return "PIDController";
  }

  public PIDController getController() {
    return controller;
  }

  public double getP() {
    return controller.getP();
  }

  public double getI() {
    return controller.getI();
  }

  public double getD() {
    return controller.getD();
  }

  public double getSetpoint() {
    return controller.getSetpoint();
  }

  @Override
  public void publish() {
    NetworkTable table = table();
    table.getEntry(".type").setString("PIDController");
    table.getEntry("p").setDouble(controller.getP());
    table.getEntry("i").setDouble(controller.getI());
    table.getEntry("d").setDouble(controller.getD());
    table.getEntry("setpoint").setDouble(controller.getSetpoint());
  }

  @Override
  public List<NetworkTableListener> listen(Runnable onUpdate) {
    publish();
    NetworkTable table = table();
    List<NetworkTableListener> listeners = new ArrayList<>();
    listeners.add(listenDouble(table.getEntry("p"), value -> controller.setP(value), onUpdate));
    listeners.add(listenDouble(table.getEntry("i"), value -> controller.setI(value), onUpdate));
    listeners.add(listenDouble(table.getEntry("d"), value -> controller.setD(value), onUpdate));
    listeners.add(
        listenDouble(table.getEntry("setpoint"), value -> controller.setSetpoint(value), onUpdate));
    return listeners;
  }

  private NetworkTable table() {
    String normalized = TopicPath.normalize(topic);
    String tablePath = normalized.startsWith("/") ? normalized.substring(1) : normalized;
    return NetworkTableInstance.getDefault().getTable(tablePath);
  }

  private static NetworkTableListener listenDouble(
      NetworkTableEntry entry, DoubleConsumer consumer, Runnable onUpdate) {
    return NetworkTableListener.createListener(
        entry,
        REMOTE_VALUE_EVENTS,
        event -> {
          if (event.valueData == null) {
            return;
          }
          consumer.accept(event.valueData.value.getDouble());
          onUpdate.run();
        });
  }

  @FunctionalInterface
  private interface DoubleConsumer {
    void accept(double value);
  }
}
