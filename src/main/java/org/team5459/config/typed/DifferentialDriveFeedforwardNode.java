package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.controller.DifferentialDriveFeedforward;
import java.util.Map;

/** A {@link DifferentialDriveFeedforward} backed by typed gain fields. */
public final class DifferentialDriveFeedforwardNode
    extends ValueConfigNode<DifferentialDriveFeedforward> {

  @JsonCreator
  public DifferentialDriveFeedforwardNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "DifferentialDriveFeedforward";
  }

  @Override
  protected DifferentialDriveFeedforward buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new DifferentialDriveFeedforward(
        fieldReader.readDouble("kVLinear", 0.0),
        fieldReader.readDouble("kALinear", 0.0),
        fieldReader.readDouble("kVAngular", 0.0),
        fieldReader.readDouble("kAAngular", 0.0),
        fieldReader.readDouble("trackwidth", 0.0));
  }

  public DifferentialDriveFeedforward getFeedforward() {
    return getValue();
  }
}
