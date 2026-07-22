package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Organizational container that may hold any registered {@link ConfigNode} types. */
public final class FolderNode extends ConfigNode {

  private final Map<String, ConfigNode> children;

  @JsonCreator
  public FolderNode(@JsonProperty("value") Map<String, ConfigNode> children) {
    this.children = children == null ? new LinkedHashMap<>() : new LinkedHashMap<>(children);
  }

  @JsonProperty("value")
  public Map<String, ConfigNode> getChildren() {
    return Collections.unmodifiableMap(children);
  }

  @JsonIgnore
  @Override
  public Map<String, ConfigNode> getChildEntries() {
    return children;
  }
}
