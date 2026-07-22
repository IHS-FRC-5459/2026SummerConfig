package org.team5459.config.typed;

import static edu.wpi.first.units.Units.Kilograms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.units.measure.Mass;
import java.util.Map;

/** A {@link Mass} measure backed by a typed {@code kg} field. */
public final class MassNode extends ValueConfigNode<Mass> {

  @JsonCreator
  public MassNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Mass";
  }

  @Override
  protected Mass buildValue() {
    return Kilograms.of(reader().readDouble("kg", 0.0));
  }

  public Mass getMeasure() {
    return getValue();
  }
}
