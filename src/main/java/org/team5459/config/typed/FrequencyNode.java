package org.team5459.config.typed;

import static edu.wpi.first.units.Units.Hertz;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.units.measure.Frequency;
import java.util.Map;

/** A {@link Frequency} measure backed by a typed {@code hz} field. */
public final class FrequencyNode extends ValueConfigNode<Frequency> {

  @JsonCreator
  public FrequencyNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Frequency";
  }

  @Override
  protected Frequency buildValue() {
    return Hertz.of(reader().readDouble("hz", 0.0));
  }

  public Frequency getMeasure() {
    return getValue();
  }
}
