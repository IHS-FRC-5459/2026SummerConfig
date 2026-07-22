package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import org.team5459.config.ConfigNode;

/** An array of {@code double} values. */
public final class DoubleArrayNode extends ConfigNode {

  private double[] value;

  @JsonCreator
  public DoubleArrayNode(@JsonProperty("value") double[] value) {
    this.value = value == null ? new double[0] : Arrays.copyOf(value, value.length);
  }

  @JsonProperty("value")
  public double[] getValue() {
    return Arrays.copyOf(value, value.length);
  }

  public void setValue(double[] value) {
    this.value = value == null ? new double[0] : Arrays.copyOf(value, value.length);
  }
}
