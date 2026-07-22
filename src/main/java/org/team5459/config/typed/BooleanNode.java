package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** A {@code boolean} configuration value. */
public final class BooleanNode extends ConfigNode {

  private boolean value;

  @JsonCreator
  public BooleanNode(@JsonProperty("value") boolean value) {
    this.value = value;
  }

  @JsonProperty("value")
  public boolean getValue() {
    return value;
  }

  public void setValue(boolean value) {
    this.value = value;
  }
}
