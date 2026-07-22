package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.team5459.config.ConfigNode;

/** A {@code double} configuration value. */
public final class DoubleNode extends ConfigNode {

  private double value;

  @JsonCreator
  public DoubleNode(@JsonProperty("value") double value) {
    this.value = value;
  }

  @JsonProperty("value")
  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }
}
