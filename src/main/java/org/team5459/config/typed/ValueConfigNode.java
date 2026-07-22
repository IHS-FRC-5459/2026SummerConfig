package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;

/** Composite node that rebuilds an immutable value whenever its fields change. */
public abstract class ValueConfigNode<T> extends CompositeConfigNode {

  private T value;

  protected ValueConfigNode(Map<String, ConfigNode> fields) {
    super(fields);
  }

  @JsonIgnore
  public T getValue() {
    return value;
  }

  @Override
  protected void syncValue() {
    value = buildValue();
  }

  protected abstract T buildValue();

  protected abstract String typeName();

  protected ConfigFieldReader reader() {
    return reader(typeName());
  }
}
