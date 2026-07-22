package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.team5459.config.ConfigNode;

/** A {@code String} configuration value. */
public final class StringNode extends ConfigNode {

  private String value;

  @JsonCreator
  public StringNode(@JsonProperty("value") String value) {
    this.value = value == null ? "" : value;
  }

  @JsonProperty("value")
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value == null ? "" : value;
  }
}
