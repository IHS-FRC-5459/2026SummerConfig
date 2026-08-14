package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.team5459.config.ConfigNode;

/**
 * Organizational folder whose JSON {@code value} is a map of named child entries.
 *
 * <p>Folders exist purely for path grouping. They do not carry runtime state beyond their children
 * and publish as NetworkTables subtables with a {@code .type=Folder} marker so empty folders still
 * exist on the network (NT cannot represent an empty table otherwise).
 */
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
