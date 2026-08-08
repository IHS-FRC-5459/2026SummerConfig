package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.controller.PIDController;
import java.util.Map;
import org.team5459.config.CompositeConfigNode;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;

/**
 * Live {@link PIDController} backed by typed {@code p}, {@code i}, {@code d}, and {@code setpoint}
 * child fields.
 *
 * <p>Unlike {@link ValueConfigNode} types, this node keeps one mutable controller instance for the
 * lifetime of the document. {@link #getController()} always returns that same object, which makes
 * it safe for subsystems to hold a reference across loops and NetworkTables edits.
 */
public final class PIDControllerNode extends CompositeConfigNode {

  private final PIDController controller = new PIDController(0.0, 0.0, 0.0);

  @JsonCreator
  public PIDControllerNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @JsonIgnore
  public PIDController getController() {
    return controller;
  }

  @Override
  protected void syncValue() {
    ensureDoubleField("p", 0.0);
    ensureDoubleField("i", 0.0);
    ensureDoubleField("d", 0.0);
    ensureDoubleField("setpoint", 0.0);
    ConfigFieldReader fieldReader = reader("PIDController");
    controller.setPID(
        fieldReader.readDouble("p", 0.0),
        fieldReader.readDouble("i", 0.0),
        fieldReader.readDouble("d", 0.0));
    controller.setSetpoint(fieldReader.readDouble("setpoint", 0.0));
  }

  /** Re-applies the current field values to the live controller. */
  public void syncController() {
    syncValue();
  }
}
