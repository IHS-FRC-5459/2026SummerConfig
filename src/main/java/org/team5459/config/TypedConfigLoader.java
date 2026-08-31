package org.team5459.config;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entry point for reading typed JSON configuration files.
 *
 * <p>After Jackson parses the root object map, every node tree is initialized so composite values
 * and live controllers reflect the loaded field data before the {@link ConfigDocument} is returned.
 */
public final class TypedConfigLoader {

  private TypedConfigLoader() {}

  /**
   * Reads a typed JSON configuration file.
   *
   * <p>Throws if the file is missing, unreadable, or malformed. For a version that degrades
   * gracefully instead of throwing, use {@link #loadSafely(File)}.
   *
   * @param jsonFile JSON file to load
   * @return Parsed configuration document
   */
  public static ConfigDocument load(File jsonFile) {
    try {
      Map<String, ConfigNode> root =
          TypedConfigMapper.mapper()
              .readValue(jsonFile, new TypeReference<LinkedHashMap<String, ConfigNode>>() {});
      root.values().forEach(ConfigNode::initializeTree);
      return new ConfigDocument(root);
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Unable to read typed config file: " + jsonFile.getAbsolutePath(), exception);
    }
  }

  /**
   * Reads a typed JSON configuration file, never throwing.
   *
   * <p>Tries {@code jsonFile} first. If it is missing or malformed (e.g. hand-edited into invalid
   * JSON), falls back to a {@code .bak} sibling file left by a previous {@link
   * TypedConfigSaver#saveSafely}. If that also fails or does not exist, falls back to an empty
   * configuration document, so every getter on the resulting {@link ConfigDocument} returns the
   * caller-supplied default rather than the robot program failing to start.
   *
   * <p>Every fallback step logs a warning through {@link ConfigWarnings} so a bad file is visible
   * in driver station logs, even though the robot keeps running.
   *
   * @param jsonFile JSON file to load
   * @return Parsed configuration document, or an empty one if nothing could be read
   */
  public static ConfigDocument loadSafely(File jsonFile) {
    try {
      return load(jsonFile);
    } catch (RuntimeException primaryFailure) {
      ConfigWarnings.warn(
          "Failed to load "
              + jsonFile.getAbsolutePath()
              + " ("
              + primaryFailure.getMessage()
              + "). Trying backup file.");
    }

    File backupFile =
        new File(jsonFile.getAbsoluteFile().getParentFile(), jsonFile.getName() + ".bak");
    try {
      ConfigDocument backupDocument = load(backupFile);
      ConfigWarnings.warn("Loaded " + backupFile.getAbsolutePath() + " instead.");
      return backupDocument;
    } catch (RuntimeException backupFailure) {
      ConfigWarnings.warn(
          "Backup file "
              + backupFile.getAbsolutePath()
              + " also unavailable ("
              + backupFailure.getMessage()
              + "). Falling back to empty configuration; all getters will use their"
              + " caller-supplied defaults.");
    }

    return new ConfigDocument(new LinkedHashMap<>());
  }
}
