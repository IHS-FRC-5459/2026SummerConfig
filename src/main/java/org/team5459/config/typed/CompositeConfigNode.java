package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Base class for typed config entries backed by named child nodes. */
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
