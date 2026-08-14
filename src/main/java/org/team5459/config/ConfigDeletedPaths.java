package org.team5459.config;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Paths removed via the Delete panel. Auto-register ignores these (and their descendants) so stale
 * NetworkTables topics from Elastic cannot recreate deleted entries. Create clears suppression for
 * a path when the user intentionally adds it again.
 */
public final class ConfigDeletedPaths {
  private static final Set<String> SUPPRESSED = ConcurrentHashMap.newKeySet();

  private ConfigDeletedPaths() {}

  /** Suppress {@code path} and treat all descendants as deleted for auto-register. */
  public static void suppress(String path) {
    if (path == null || path.isBlank()) {
      return;
    }
    SUPPRESSED.add(normalize(path));
  }

  /** Allow auto-register / intentional create at {@code path} again. */
  public static void allow(String path) {
    if (path == null || path.isBlank()) {
      return;
    }
    String normalized = normalize(path);
    SUPPRESSED.removeIf(
        suppressed ->
            normalized.equals(suppressed)
                || normalized.startsWith(suppressed + "/")
                || suppressed.startsWith(normalized + "/"));
  }

  /** True if {@code path} was deleted and should not be auto-registered. */
  public static boolean isSuppressed(String path) {
    if (path == null || path.isBlank()) {
      return false;
    }
    String normalized = normalize(path);
    for (String suppressed : SUPPRESSED) {
      if (normalized.equals(suppressed) || normalized.startsWith(suppressed + "/")) {
        return true;
      }
    }
    return false;
  }

  private static String normalize(String path) {
    String normalized = path.trim().replace('\\', '/');
    while (normalized.startsWith("/")) {
      normalized = normalized.substring(1);
    }
    if (normalized.startsWith("Config/")) {
      normalized = normalized.substring("Config/".length());
    }
    return normalized;
  }
}
