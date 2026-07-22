package org.team5459.config.types;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Radians;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.units.measure.Angle;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** An {@link Angle} measure backed by typed {@code deg} or {@code rad} fields. */
public final class AngleNode extends ValueConfigNode<Angle> {

  @JsonCreator
  public AngleNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Angle";
  }

  @Override
  protected Angle buildValue() {
    ConfigFieldReader fieldReader = reader();
    if (getFields().containsKey("deg")) {
      return Degrees.of(fieldReader.readDouble("deg", 0.0));
    }
    return Radians.of(fieldReader.readDouble("rad", 0.0));
  }

  public Angle getMeasure() {
    return getValue();
  }
}
