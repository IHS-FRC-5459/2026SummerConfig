package org.team5459.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * hhhh(for blank commit) Config entry whose JSON {@code value} is an object of named child nodes.
 *
 * <p>Composite nodes are the bridge between typed JSON fields and runtime objects. During {@link
 * #initialize()}, every child is initialized first, then {@link #syncValue()} rebuilds the parent's
 * runtime state from the current child values.
 *
 * <p>When NetworkTables updates a scalar child, {@link #applyFieldChanges()} re-runs {@link
 * #syncValue()} so parent objects (poses, feedforwards, constraints, etc.) stay consistent.
 */
public abstract class CompositeConfigNode extends ConfigNode {

  private final Map<String, ConfigNode> fields;

  protected CompositeConfigNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    this.fields = fields == null ? new LinkedHashMap<>() : new LinkedHashMap<>(fields);
  }

  @JsonProperty("value")
  public Map<String, ConfigNode> getFields() {
    return Collections.unmodifiableMap(fields);
  }

  @JsonIgnore
  @Override
  public Map<String, ConfigNode> getChildEntries() {
    return fields;
  }

  @Override
  public void initialize() {
    fields.values().forEach(ConfigNode::initializeTree);
    syncValue();
  }

  /** Rebuilds runtime state from the current child field values. */
  protected abstract void syncValue();

  /** Rebuilds runtime state after remote NetworkTables updates to child fields. */
  public void applyFieldChanges() {
    syncValue();
  }

  protected ConfigFieldReader reader(String typeName) {
    return new ConfigFieldReader(fields, typeName);
  }
}
