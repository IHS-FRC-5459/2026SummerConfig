package org.team5459.config.typed;

import static edu.wpi.first.units.Units.Meters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.units.measure.Distance;
import java.util.Map;

/** A {@link Distance} measure backed by a typed {@code m} field. */
public final class DistanceNode extends ValueConfigNode<Distance> {

  @JsonCreator
  public DistanceNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Distance";
  }

  @Override
  protected Distance buildValue() {
    return Meters.of(reader().readDouble("m", 0.0));
  }

  public Distance getMeasure() {
    return getValue();
  }
}
