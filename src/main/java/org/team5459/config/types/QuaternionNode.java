package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Quaternion;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** A {@link Quaternion} backed by typed {@code w}, {@code x}, {@code y}, and {@code z} fields. */
public final class QuaternionNode extends ValueConfigNode<Quaternion> {

  @JsonCreator
  public QuaternionNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Quaternion";
  }

  @Override
  protected Quaternion buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new Quaternion(
        fieldReader.readDouble("w", 1.0),
        fieldReader.readDouble("x", 0.0),
        fieldReader.readDouble("y", 0.0),
        fieldReader.readDouble("z", 0.0));
  }

  public Quaternion getQuaternion() {
    return getValue();
  }
}
