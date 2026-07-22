package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.controller.PIDController;
import java.util.Map;

/** A live {@link PIDController} backed by typed {@code p}, {@code i}, and {@code d} fields. */
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
    ConfigFieldReader fieldReader = reader("PIDController");
    controller.setPID(
        fieldReader.readDouble("p", 0.0),
        fieldReader.readDouble("i", 0.0),
        fieldReader.readDouble("d", 0.0));
  }

  /** Re-applies the current field values to the live controller. */
  public void syncController() {
    syncValue();
  }
}
