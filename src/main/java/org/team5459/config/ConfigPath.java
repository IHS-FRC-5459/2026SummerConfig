package org.team5459.config;

import java.util.Map;

/**
 * Resolves slash-separated paths such as {@code Arm/PIDController/p} against a config tree.
 *
 * <p>Path resolution walks folder and composite child maps segment by segment. If any segment is
 * missing, blank, or reached through a node that cannot contain children, {@link ConfigWarnings}
 * logs a message and {@code null} is returned so callers can fall back to defaults.
 */
final class ConfigPath {

  private ConfigPath() {}

  static ConfigNode resolve(Map<String, ConfigNode> root, String path) {
    if (path == null || path.isBlank()) {
      ConfigWarnings.warnMissingPath(path);
      return null;
    }

    String[] parts = path.split("/");
    Map<String, ConfigNode> current = root;
    ConfigNode node = null;

    for (int index = 0; index < parts.length; index++) {
      String part = parts[index];
      if (part.isBlank()) {
        ConfigWarnings.warnMissingPath(path);
        return null;
      }

      node = current.get(part);
      if (node == null) {
        ConfigWarnings.warnMissingPath(path);
        return null;
      }

      if (index < parts.length - 1) {
        Map<String, ConfigNode> childEntries = node.getChildEntries();
        if (childEntries == null) {
          ConfigWarnings.warnNotNavigable(path, part);
          return null;
        }
        current = childEntries;
      }
    }

    return node;
  }
}
