package org.team5459.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;

/**
 * Composite node that caches an immutable {@code T} rebuilt from child fields.
 *
 * <p>Subclasses implement {@link #buildValue()} once; {@link #syncValue()} stores the result in
 * {@link #getValue()}. Use the typed accessor on the node (for example {@code getPose()}) or the
 * matching {@link ConfigDocument} getter at the document level.
 *
 * <p>Unlike live controller nodes, each sync produces a fresh value object. Remote edits to child
 * scalars therefore propagate automatically without extra subsystem code.
 */
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
