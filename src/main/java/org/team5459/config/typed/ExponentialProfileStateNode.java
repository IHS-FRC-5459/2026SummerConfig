package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.trajectory.ExponentialProfile;
import java.util.Map;

/** An {@link ExponentialProfile.State} backed by typed position and velocity fields. */
public final class ExponentialProfileStateNode extends ValueConfigNode<ExponentialProfile.State> {

  @JsonCreator
  public ExponentialProfileStateNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "ExponentialProfile.State";
  }

  @Override
  protected ExponentialProfile.State buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new ExponentialProfile.State(
        fieldReader.readDouble("position", 0.0), fieldReader.readDouble("velocity", 0.0));
  }

  public ExponentialProfile.State getState() {
    return getValue();
  }
}
