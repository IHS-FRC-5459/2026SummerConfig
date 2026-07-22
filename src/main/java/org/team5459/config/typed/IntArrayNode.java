package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

/** An array of {@code int} values. */
public final class IntArrayNode extends ConfigNode {

  private int[] value;

  @JsonCreator
  public IntArrayNode(@JsonProperty("value") int[] value) {
    this.value = value == null ? new int[0] : Arrays.copyOf(value, value.length);
  }

  @JsonProperty("value")
  public int[] getValue() {
    return Arrays.copyOf(value, value.length);
  }

  public void setValue(int[] value) {
    this.value = value == null ? new int[0] : Arrays.copyOf(value, value.length);
  }
}
