package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import java.util.Map;

/** A {@link TrapezoidProfile.State} backed by typed position and velocity fields. */
public final class TrapezoidProfileStateNode extends ValueConfigNode<TrapezoidProfile.State> {

  @JsonCreator
  public TrapezoidProfileStateNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "TrapezoidProfile.State";
  }

  @Override
  protected TrapezoidProfile.State buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new TrapezoidProfile.State(
        fieldReader.readDouble("position", 0.0), fieldReader.readDouble("velocity", 0.0));
  }

  public TrapezoidProfile.State getState() {
    return getValue();
  }
}
