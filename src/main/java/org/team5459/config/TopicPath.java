package org.team5459.config;

/**
 * Normalizes Elastic {@code properties.topic} strings and robot getter paths.
 *
 * <p>Topics are stored as absolute NetworkTables paths such as {@code /Config/Arm/PIDController}.
 * Getters may pass that full topic, or a path relative to {@code /Config} such as {@code
 * Arm/PIDController}.
 */
public final class TopicPath {

  public static final String CONFIG_ROOT = "/Config";

  private TopicPath() {}

  /** Returns a canonical absolute topic path starting with {@code /}. */
  public static String normalize(String topicOrPath) {
    if (topicOrPath == null || topicOrPath.isBlank()) {
      return "";
    }
    String trimmed = topicOrPath.trim();
    if (trimmed.startsWith("/")) {
      return trimmed;
    }
    if (trimmed.startsWith("Config/") || trimmed.equals("Config")) {
      return "/" + trimmed;
    }
    return CONFIG_ROOT + "/" + trimmed;
  }

  /**
   * Splits an absolute topic into parent table path (no leading slash) and entry name.
   *
   * <p>Example: {@code /Config/Arm/p} → table {@code Config/Arm}, entry {@code p}.
   */
  public static TableEntry split(String absoluteTopic) {
    String normalized = normalize(absoluteTopic);
    if (normalized.isEmpty() || normalized.equals("/")) {
      return new TableEntry("", "");
    }
    String withoutSlash = normalized.startsWith("/") ? normalized.substring(1) : normalized;
    int lastSlash = withoutSlash.lastIndexOf('/');
    if (lastSlash < 0) {
      return new TableEntry("", withoutSlash);
    }
    return new TableEntry(
        withoutSlash.substring(0, lastSlash), withoutSlash.substring(lastSlash + 1));
  }

  /** Parent table path (NT table key) and leaf entry name. */
  public record TableEntry(String tablePath, String entryName) {}
}
