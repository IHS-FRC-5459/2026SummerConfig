package org.team5459.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Creates a new config entry by cloning a curated {@code templates/template*} node into a
 * destination path.
 */
public final class ConfigCreateHelper {

  /** Display label → template path under the config document. */
  public static final Map<String, String> TYPE_TO_TEMPLATE;

  static {
    Map<String, String> types = new LinkedHashMap<>();
    types.put("Double", "templates/templateDouble");
    types.put("Int", "templates/templateInt");
    types.put("Boolean", "templates/templateBoolean");
    types.put("String", "templates/templateString");
    types.put("DoubleArray", "templates/templateDoubleArray");
    types.put("IntArray", "templates/templateIntArray");
    types.put("PIDController", "templates/templatePIDController");
    types.put("Rotation2d", "templates/templateRotation2d");
    types.put("Translation2d", "templates/templateTranslation2d");
    types.put("Pose2d", "templates/templatePose2d");
    types.put("SimpleMotorFeedforward", "templates/templateSimpleMotorFeedforward");
    types.put("ArmFeedforward", "templates/templateArmFeedforward");
    types.put("ElevatorFeedforward", "templates/templateElevatorFeedforward");
    TYPE_TO_TEMPLATE = Collections.unmodifiableMap(types);
  }

  public static final String DEFAULT_TYPE = "Double";

  private static final Set<String> RESERVED_TOP_LEVEL =
      Set.of(
          ConfigSaveButton.kDefaultEntryName.toLowerCase(Locale.ROOT),
          ConfigDebugMode.kDefaultEntryName.toLowerCase(Locale.ROOT),
          ConfigCreatePanel.SUBTABLE.toLowerCase(Locale.ROOT));

  private ConfigCreateHelper() {}

  /**
   * Clones the template for {@code typeLabel} into {@code path}.
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
    if (isReserved(normalized) || isUnderTemplates(normalized)) {
      ConfigWarnings.warn("Create ignored invalid path: " + path);
      return false;
    }
    if (document.hasPath(normalized)) {
      ConfigWarnings.warn("Create path already exists: " + normalized);
      return false;
    }

    String label = typeLabel == null || typeLabel.isBlank() ? DEFAULT_TYPE : typeLabel.trim();
    String templatePath = TYPE_TO_TEMPLATE.get(label);
    if (templatePath == null) {
      ConfigWarnings.warn("Create ignored unknown type: " + label);
      return false;
    }

    ConfigNode template = document.getNodeQuiet(templatePath);
    if (template == null) {
      ConfigWarnings.warn("Create template missing: " + templatePath);
      return false;
    }

    ConfigNode clone = ConfigNodeCloner.deepCopy(template);
    if (!document.insertLeaf(normalized, clone)) {
      ConfigWarnings.warn("Create failed to insert: " + normalized);
      return false;
    }

    System.out.println("[Config] Created " + label + " at /Config/" + normalized);
    return true;
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

  private static boolean isUnderTemplates(String relativePath) {
    return relativePath.equals("templates") || relativePath.startsWith("templates/");
  }

  private static boolean isReserved(String relativePath) {
    String top = relativePath.split("/", 2)[0];
    return RESERVED_TOP_LEVEL.contains(top.toLowerCase(Locale.ROOT));
  }
}
