package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.controller.ArmFeedforward;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** An {@link ArmFeedforward} backed by typed gain fields. */
public final class ArmFeedforwardNode extends ValueConfigNode<ArmFeedforward> {

  @JsonCreator
  public ArmFeedforwardNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "ArmFeedforward";
  }

  @Override
  protected ArmFeedforward buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new ArmFeedforward(
        fieldReader.readDouble("ks", 0.0),
        fieldReader.readDouble("kg", 0.0),
        fieldReader.readDouble("kv", 0.0),
        fieldReader.readDouble("ka", 0.0),
        fieldReader.readDouble("dtSeconds", 0.02));
  }

  public ArmFeedforward getFeedforward() {
    return getValue();
  }
}
