package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** A {@link TrapezoidProfile.Constraints} backed by typed velocity and acceleration fields. */
public final class TrapezoidProfileConstraintsNode
    extends ValueConfigNode<TrapezoidProfile.Constraints> {

  @JsonCreator
  public TrapezoidProfileConstraintsNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "TrapezoidProfile.Constraints";
  }

  @Override
  protected TrapezoidProfile.Constraints buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new TrapezoidProfile.Constraints(
        fieldReader.readDouble("maxVelocity", 0.0), fieldReader.readDouble("maxAcceleration", 0.0));
  }

  public TrapezoidProfile.Constraints getConstraints() {
    return getValue();
  }
}
