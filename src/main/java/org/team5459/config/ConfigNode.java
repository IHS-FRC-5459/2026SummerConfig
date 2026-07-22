package org.team5459.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Map;
import org.team5459.config.types.FolderNode;

/**
 * A single typed entry in a configuration file.
 *
 * <p>Each JSON object contains a {@code type} discriminator and a {@code value} payload. Jackson
 * maps the discriminator to a concrete node class registered in {@link ConfigTypeRegistry}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    fieldVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.DEFAULT)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public abstract class ConfigNode {

  /** Initializes runtime state after JSON deserialization. */
  public void initialize() {}

  /**
   * Returns named child entries for path traversal, or {@code null} if this node cannot contain
   * nested paths.
   */
  @JsonIgnore
  public Map<String, ConfigNode> getChildEntries() {
    return null;
  }

  /** Initializes runtime state for every node in a subtree. */
  public static void initializeTree(ConfigNode node) {
    if (node instanceof FolderNode folder) {
      folder.getChildren().values().forEach(ConfigNode::initializeTree);
    } else if (node instanceof CompositeConfigNode composite) {
      composite.initialize();
    } else {
      node.initialize();
    }
  }
}
