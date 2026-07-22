package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** A {@link SimpleMotorFeedforward} backed by typed gain fields. */
public final class SimpleMotorFeedforwardNode extends ValueConfigNode<SimpleMotorFeedforward> {

  @JsonCreator
  public SimpleMotorFeedforwardNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "SimpleMotorFeedforward";
  }

  @Override
  protected SimpleMotorFeedforward buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new SimpleMotorFeedforward(
        fieldReader.readDouble("ks", 0.0),
        fieldReader.readDouble("kv", 0.0),
        fieldReader.readDouble("ka", 0.0),
        fieldReader.readDouble("dtSeconds", 0.02));
  }

  public SimpleMotorFeedforward getFeedforward() {
    return getValue();
  }
}
