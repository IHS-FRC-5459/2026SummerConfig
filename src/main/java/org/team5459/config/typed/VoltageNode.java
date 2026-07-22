package org.team5459.config.typed;

import static edu.wpi.first.units.Units.Volts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.units.measure.Voltage;
import java.util.Map;

/** A {@link Voltage} measure backed by a typed {@code v} field. */
public final class VoltageNode extends ValueConfigNode<Voltage> {

  @JsonCreator
  public VoltageNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Voltage";
  }

  @Override
  protected Voltage buildValue() {
    return Volts.of(reader().readDouble("v", 0.0));
  }

  public Voltage getMeasure() {
    return getValue();
  }
}
