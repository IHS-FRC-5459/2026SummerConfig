package org.team5459.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.team5459.config.types.ArmFeedforwardNode;
import org.team5459.config.types.BooleanNode;
import org.team5459.config.types.DoubleArrayNode;
import org.team5459.config.types.DoubleNode;
import org.team5459.config.types.ElevatorFeedforwardNode;
import org.team5459.config.types.FolderNode;
import org.team5459.config.types.IntArrayNode;
import org.team5459.config.types.IntNode;
import org.team5459.config.types.PIDControllerNode;
import org.team5459.config.types.Pose2dNode;
import org.team5459.config.types.Rotation2dNode;
import org.team5459.config.types.SimpleMotorFeedforwardNode;
import org.team5459.config.types.StringNode;
import org.team5459.config.types.Translation2dNode;

/**
 * Creates a new config entry with zero/empty defaults, or an empty {@link FolderNode}, at a
 * destination path.
 */
public final class ConfigCreateHelper {

  public static final String DEFAULT_TYPE = "Double";
  public static final String FOLDER_TYPE = "Folder";
  public static final String ROOT_FOLDER = "(root)";

  /** Supported Create-panel types (Folder first, then scalars/composites). */
  public static final List<String> CREATABLE_TYPES;

  static {
    List<String> types = new ArrayList<>();
    types.add(FOLDER_TYPE);
    types.add("Double");
    types.add("Int");
    types.add("Boolean");
    types.add("String");
    types.add("DoubleArray");
    types.add("IntArray");
    types.add("PIDController");
    types.add("Rotation2d");
    types.add("Translation2d");
    types.add("Pose2d");
    types.add("SimpleMotorFeedforward");
    types.add("ArmFeedforward");
    types.add("ElevatorFeedforward");
    CREATABLE_TYPES = Collections.unmodifiableList(types);
  }

  private static final Set<String> RESERVED_TOP_LEVEL =
      Set.of(
          ConfigSaveButton.kDefaultEntryName.toLowerCase(Locale.ROOT),
          ConfigDebugMode.kDefaultEntryName.toLowerCase(Locale.ROOT),
          ConfigCreatePanel.SUBTABLE.toLowerCase(Locale.ROOT),
          ConfigDeletePanel.SUBTABLE.toLowerCase(Locale.ROOT));

  private ConfigCreateHelper() {}

  /** Type chooser options for the Create panel. */
  public static String[] typeChooserOptions() {
    return CREATABLE_TYPES.toArray(String[]::new);
  }

  /**
   * Builds a destination path from folder chooser selection + entry/folder name.
   *
   * <p>{@code folderSelection} may be {@link #ROOT_FOLDER} or a nested path such as {@code
   * Claw/Intake}.
   *
   * @return normalized path, or {@code null} if invalid
   */
  public static String buildPath(String folderSelection, String entryName) {
    String name = sanitizeLeaf(entryName);
    if (name == null) {
      return null;
    }

    String folder =
        folderSelection == null ? ROOT_FOLDER : folderSelection.trim().replace('\\', '/');
    if (folder.isBlank() || ROOT_FOLDER.equals(folder)) {
      return name;
    }
    String normalizedFolder = normalizeFolderPath(folder);
    if (normalizedFolder == null) {
      return null;
    }
    return normalizedFolder + "/" + name;
  }

  /**
   * All folder paths in the document (any depth), e.g. {@code Claw}, {@code Claw/Intake}. Excludes
   * reserved top-level names.
   */
  public static List<String> listCreatableFolders(ConfigDocument document) {
    List<String> folders = new ArrayList<>();
    if (document == null) {
      return folders;
    }
    collectFolders(document.getRootEntries(), "", folders);
    Collections.sort(folders, String.CASE_INSENSITIVE_ORDER);
    return folders;
  }

  private static void collectFolders(
      Map<String, ConfigNode> entries, String prefix, List<String> out) {
    for (Map.Entry<String, ConfigNode> entry : entries.entrySet()) {
      String key = entry.getKey();
      if (!(entry.getValue() instanceof FolderNode folderNode)) {
        continue;
      }
      if (prefix.isEmpty() && isReservedTop(key)) {
        continue;
      }
      String path = prefix.isEmpty() ? key : prefix + "/" + key;
      out.add(path);
      collectFolders(folderNode.getChildEntries(), path, out);
    }
  }

  /** Options for the Folder String Chooser: {@code (root)} plus every folder path. */
  public static String[] folderChooserOptions(ConfigDocument document) {
    List<String> options = new ArrayList<>();
    options.add(ROOT_FOLDER);
    options.addAll(listCreatableFolders(document));
    return options.toArray(String[]::new);
  }

  /**
   * Creates a default node of {@code typeLabel} at {@code path}.
   *
   * @return {@code true} if inserted
   */
  public static boolean create(ConfigDocument document, String typeLabel, String path) {
    if (document == null) {
      return false;
    }
    String normalized = normalizeUserPath(path);
    if (normalized == null) {
      ConfigWarnings.warn("Create ignored: empty path");
      return false;
    }
    if (isReserved(normalized)) {
      ConfigWarnings.warn("Create ignored invalid path: " + path);
      return false;
    }
    if (document.hasPath(normalized)) {
      ConfigWarnings.warn("Create path already exists: " + normalized);
      return false;
    }

    String label = typeLabel == null || typeLabel.isBlank() ? DEFAULT_TYPE : typeLabel.trim();
    ConfigNode node = createDefaultNode(label);
    if (node == null) {
      ConfigWarnings.warn("Create ignored unknown type: " + label);
      return false;
    }
    if (!document.insertLeaf(normalized, node)) {
      ConfigWarnings.warn("Create failed to insert: " + normalized);
      return false;
    }

    ConfigDeletedPaths.allow(normalized);
    System.out.println("[Config] Created " + label + " at /Config/" + normalized);
    return true;
  }

  static ConfigNode createDefaultNode(String typeLabel) {
    return switch (typeLabel) {
      case FOLDER_TYPE -> new FolderNode(new LinkedHashMap<>());
      case "Double" -> new DoubleNode(0.0);
      case "Int" -> new IntNode(0);
      case "Boolean" -> new BooleanNode(false);
      case "String" -> new StringNode("");
      case "DoubleArray" -> new DoubleArrayNode(new double[] {0.0});
      case "IntArray" -> new IntArrayNode(new int[] {0});
      case "PIDController" -> new PIDControllerNode(doubleFields("p", "i", "d", "setpoint"));
      case "Rotation2d" -> new Rotation2dNode(doubleFields("deg"));
      case "Translation2d" -> new Translation2dNode(doubleFields("x", "y"));
      case "Pose2d" -> new Pose2dNode(doubleFields("x", "y", "deg"));
      case "SimpleMotorFeedforward" -> new SimpleMotorFeedforwardNode(
          doubleFields("ks", "kv", "ka"));
      case "ArmFeedforward" -> new ArmFeedforwardNode(doubleFields("ks", "kg", "kv", "ka"));
      case "ElevatorFeedforward" -> new ElevatorFeedforwardNode(
          doubleFields("ks", "kg", "kv", "ka"));
      default -> null;
    };
  }

  private static Map<String, ConfigNode> doubleFields(String... names) {
    Map<String, ConfigNode> fields = new LinkedHashMap<>();
    for (String name : names) {
      fields.put(name, new DoubleNode(0.0));
    }
    return fields;
  }

  static String normalizeUserPath(String path) {
    if (path == null || path.isBlank()) {
      return null;
    }
    String normalized = path.trim().replace('\\', '/');
    while (normalized.startsWith("/")) {
      normalized = normalized.substring(1);
    }
    if (normalized.startsWith("Config/")) {
      normalized = normalized.substring("Config/".length());
    }
    return normalized.isBlank() ? null : normalized;
  }

  private static String sanitizeLeaf(String leaf) {
    if (leaf == null || leaf.isBlank()) {
      return null;
    }
    String trimmed = leaf.trim();
    if (trimmed.contains("/") || trimmed.contains("\\")) {
      return null;
    }
    return trimmed;
  }

  /** Validates a multi-segment folder path like {@code Claw/Intake}. */
  private static String normalizeFolderPath(String folder) {
    String[] parts = folder.split("/");
    if (parts.length == 0) {
      return null;
    }
    StringBuilder path = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      String part = sanitizeLeaf(parts[i]);
      if (part == null) {
        return null;
      }
      if (i == 0 && isReservedTop(part)) {
        return null;
      }
      if (i > 0) {
        path.append('/');
      }
      path.append(part);
    }
    return path.toString();
  }

  private static boolean isReserved(String relativePath) {
    return isReservedTop(relativePath.split("/", 2)[0]);
  }

  private static boolean isReservedTop(String top) {
    return RESERVED_TOP_LEVEL.contains(top.toLowerCase(Locale.ROOT));
  }
}
