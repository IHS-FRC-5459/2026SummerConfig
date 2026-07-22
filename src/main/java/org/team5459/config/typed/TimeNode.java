package org.team5459.config.typed;

import static edu.wpi.first.units.Units.Seconds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.units.measure.Time;
import java.util.Map;

/** A {@link Time} measure backed by a typed {@code s} field. */
public final class TimeNode extends ValueConfigNode<Time> {

  @JsonCreator
  public TimeNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Time";
  }

  @Override
  protected Time buildValue() {
    return Seconds.of(reader().readDouble("s", 0.0));
  }

  public Time getMeasure() {
    return getValue();
  }
}
