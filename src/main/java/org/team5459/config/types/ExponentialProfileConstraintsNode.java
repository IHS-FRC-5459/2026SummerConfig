package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.trajectory.ExponentialProfile;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** An {@link ExponentialProfile.Constraints} backed by typed input and gain fields. */
public final class ExponentialProfileConstraintsNode
    extends ValueConfigNode<ExponentialProfile.Constraints> {

  @JsonCreator
  public ExponentialProfileConstraintsNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "ExponentialProfile.Constraints";
  }

  @Override
  protected ExponentialProfile.Constraints buildValue() {
    ConfigFieldReader fieldReader = reader();
    if (getFields().containsKey("A")) {
      return ExponentialProfile.Constraints.fromStateSpace(
          fieldReader.readDouble("maxInput", 0.0),
          fieldReader.readDouble("A", 0.0),
          fieldReader.readDouble("B", 0.0));
    }
    return ExponentialProfile.Constraints.fromCharacteristics(
        fieldReader.readDouble("maxInput", 0.0),
        fieldReader.readDouble("kV", 0.0),
        fieldReader.readDouble("kA", 0.0));
  }

  public ExponentialProfile.Constraints getConstraints() {
    return getValue();
  }
}
