package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import java.util.Map;

/** A live {@link ProfiledPIDController} backed by typed PID and constraint fields. */
public final class ProfiledPIDControllerNode extends CompositeConfigNode {

  private ProfiledPIDController controller;

  @JsonCreator
  public ProfiledPIDControllerNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @JsonIgnore
  public ProfiledPIDController getController() {
    if (controller == null) {
      controller =
          new ProfiledPIDController(
              0.0, 0.0, 0.0, new TrapezoidProfile.Constraints(0.0, 0.0), 0.02);
    }
    return controller;
  }

  @Override
  protected void syncValue() {
    ConfigFieldReader fieldReader = reader("ProfiledPIDController");
    TrapezoidProfile.Constraints constraints =
        fieldReader.readTrapezoidConstraints(
            "constraints",
            new TrapezoidProfile.Constraints(
                fieldReader.readDouble("maxVelocity", 0.0),
                fieldReader.readDouble("maxAcceleration", 0.0)));
    double period = fieldReader.readDouble("period", 0.02);
    double p = fieldReader.readDouble("p", 0.0);
    double i = fieldReader.readDouble("i", 0.0);
    double d = fieldReader.readDouble("d", 0.0);
    if (controller == null) {
      controller = new ProfiledPIDController(p, i, d, constraints, period);
    } else {
      controller.setConstraints(constraints);
      controller.setPID(p, i, d);
    }
  }
}
