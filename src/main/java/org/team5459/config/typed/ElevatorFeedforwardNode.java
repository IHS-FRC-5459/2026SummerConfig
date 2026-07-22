package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.controller.ElevatorFeedforward;
import java.util.Map;

/** An {@link ElevatorFeedforward} backed by typed gain fields. */
public final class ElevatorFeedforwardNode extends ValueConfigNode<ElevatorFeedforward> {

  @JsonCreator
  public ElevatorFeedforwardNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "ElevatorFeedforward";
  }

  @Override
  protected ElevatorFeedforward buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new ElevatorFeedforward(
        fieldReader.readDouble("ks", 0.0),
        fieldReader.readDouble("kg", 0.0),
        fieldReader.readDouble("kv", 0.0),
        fieldReader.readDouble("ka", 0.0),
        fieldReader.readDouble("dtSeconds", 0.02));
  }

  public ElevatorFeedforward getFeedforward() {
    return getValue();
  }
}
