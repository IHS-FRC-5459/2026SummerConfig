package org.team5459.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.team5459.config.types.FolderNode;

/**
 * Lists deletable config paths and removes them from a {@link ConfigDocument} (debug Delete panel).
 *
 * <p>Composite nodes such as {@code PIDController} may be deleted as a whole, but their mandated
 * child fields ({@code p}/{@code i}/{@code d}/{@code setpoint}, etc.) are not individually
 * deletable.
 */
public final class ConfigDeleteHelper {

  public static final String NONE_OPTION = "(none)";

  private static final Set<String> RESERVED_TOP_LEVEL =
      Set.of(
          ConfigSaveButton.kDefaultEntryName.toLowerCase(Locale.ROOT),
          ConfigDebugMode.kDefaultEntryName.toLowerCase(Locale.ROOT),
          ConfigCreatePanel.SUBTABLE.toLowerCase(Locale.ROOT),
          "delete");

  private ConfigDeleteHelper() {}

  /**
   * Deletable config paths (folders, composites, and free-standing leaves), sorted. Reserved
   * control topics and children of composite nodes are excluded.
   */
  public static List<String> listDeletablePaths(ConfigDocument document) {
    List<String> paths = new ArrayList<>();
    if (document == null) {
      return paths;
    }
    collectPaths(document.getRootEntries(), "", paths);
    Collections.sort(paths, String.CASE_INSENSITIVE_ORDER);
    return paths;
  }

  public static String[] pathChooserOptions(ConfigDocument document) {
    List<String> options = new ArrayList<>();
    options.add(NONE_OPTION);
    options.addAll(listDeletablePaths(document));
    return options.toArray(String[]::new);
  }

  /**
   * Deletes {@code path} from the document. Deleting a folder removes all descendants. Composite
   * nodes are removed as a unit; deleting a composite child field is rejected.
   *
   * @return {@code true} if something was removed
   */
  public static boolean delete(ConfigDocument document, String path) {
    if (document == null) {
      return false;
    }
    String normalized = normalize(path);
    if (normalized == null) {
      ConfigWarnings.warn("Delete ignored: empty or invalid path '" + path + "'");
      return false;
    }
    if (isReserved(normalized)) {
      ConfigWarnings.warn("Delete ignored reserved path: " + normalized);
      return false;
    }
    if (!document.hasPath(normalized)) {
      ConfigWarnings.warn("Delete path does not exist: " + normalized);
      return false;
    }
    if (isCompositeChild(document, normalized)) {
      ConfigWarnings.warn(
          "Delete ignored: cannot remove fields of a composite type (delete the parent instead): "
              + normalized);
      return false;
    }
    boolean folder = document.getNodeQuiet(normalized) instanceof FolderNode;
    if (!document.removePath(normalized)) {
      ConfigWarnings.warn("Delete failed: " + normalized);
      return false;
    }
    ConfigDeletedPaths.suppress(normalized);
    TypedNetworkTableSync.unpublishPath(normalized);
    System.out.println(
        "[Config] Deleted "
            + (folder ? "folder (and children)" : "entry")
            + " at /Config/"
            + normalized);
    return true;
  }

  static String normalize(String path) {
    if (path == null || path.isBlank() || NONE_OPTION.equals(path.trim())) {
      return null;
    }
    String normalized = path.trim().replace('\\', '/');
    while (normalized.startsWith("/")) {
      normalized = normalized.substring(1);
    }
    if (normalized.startsWith("Config/")) {
      normalized = normalized.substring("Config/".length());
    }
    if (normalized.isBlank() || normalized.contains("//")) {
      return null;
    }
    return normalized;
  }

  private static void collectPaths(
      Map<String, ConfigNode> entries, String prefix, List<String> out) {
    for (Map.Entry<String, ConfigNode> entry : entries.entrySet()) {
      String key = entry.getKey();
      if (prefix.isEmpty() && isReservedTop(key)) {
        continue;
      }
      String path = prefix.isEmpty() ? key : prefix + "/" + key;
      ConfigNode node = entry.getValue();
      out.add(path);
      // Folders may delete children individually; composites must be deleted as a whole.
      if (node instanceof FolderNode folder) {
        collectPaths(folder.getChildren(), path, out);
      }
    }
  }

  /** True when {@code path} is a field under a {@link CompositeConfigNode} (any depth). */
  private static boolean isCompositeChild(ConfigDocument document, String path) {
    int slash = path.lastIndexOf('/');
    while (slash > 0) {
      String parentPath = path.substring(0, slash);
      ConfigNode parent = document.getNodeQuiet(parentPath);
      if (parent instanceof CompositeConfigNode) {
        return true;
      }
      if (parent instanceof FolderNode) {
        return false;
      }
      slash = parentPath.lastIndexOf('/');
    }
    return false;
  }

  private static boolean isReserved(String relativePath) {
    String first = relativePath.split("/", 2)[0];
    return isReservedTop(first);
  }

  private static boolean isReservedTop(String key) {
    return key != null && RESERVED_TOP_LEVEL.contains(key.toLowerCase(Locale.ROOT));
  }
}
