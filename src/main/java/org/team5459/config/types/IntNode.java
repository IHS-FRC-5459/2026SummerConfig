package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.team5459.config.ConfigNode;

/** An {@code int} configuration value. */
public final class IntNode extends ConfigNode {

  private int value;

  @JsonCreator
  public IntNode(@JsonProperty("value") int value) {
    this.value = value;
  }

  @JsonProperty("value")
  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
