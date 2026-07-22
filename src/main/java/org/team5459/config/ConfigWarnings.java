package org.team5459.config;

/** Logs configuration warnings without failing robot startup. */
public final class ConfigWarnings {

  private ConfigWarnings() {}

  static void warnMissingPath(String path) {
    System.err.println("[Config] Missing entry at path '" + path + "'. Using default value.");
  }

  static void warnTypeMismatch(String path, String expectedType, ConfigNode actual) {
    String actualType = actual == null ? "null" : actual.getClass().getSimpleName();
    System.err.println(
        "[Config] Expected "
            + expectedType
            + " at path '"
            + path
            + "' but found "
            + actualType
            + ". Using default value.");
  }

  static void warnNotNavigable(String path, String segment) {
    System.err.println(
        "[Config] Path segment '"
            + segment
            + "' in '"
            + path
            + "' cannot contain nested entries. Using default value.");
  }

  public static void warnMissingField(String typeName, String fieldName, Object defaultValue) {
    System.err.println(
        "[Config] "
            + typeName
            + " is missing field '"
            + fieldName
            + "'. Using default value "
            + defaultValue
            + ".");
  }

  public static void warnMissingField(String typeName, String fieldName) {
    System.err.println(
        "[Config] " + typeName + " is missing field '" + fieldName + "'. Using default value.");
  }

  public static void warnWrongFieldType(
      String typeName, String fieldName, String expectedType, ConfigNode actual) {
    String actualType = actual == null ? "null" : actual.getClass().getSimpleName();
    System.err.println(
        "[Config] "
            + typeName
            + " field '"
            + fieldName
            + "' expected "
            + expectedType
            + " but found "
            + actualType
            + ". Using default value.");
  }

  static void warnUnsupportedNetworkTablesType(String name, ConfigNode node) {
    String nodeType = node == null ? "null" : node.getClass().getSimpleName();
    System.err.println(
        "[Config] Skipping NetworkTables sync for '"
            + name
            + "' ("
            + nodeType
            + "). Only folders, composites, and scalar/array entries are supported.");
  }
}
