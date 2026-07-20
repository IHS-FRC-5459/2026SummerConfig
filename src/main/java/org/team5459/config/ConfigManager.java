package org.team5459.config;

import java.io.File;

/**
 * High-level entry point for loading configuration files.
 *
 * <p>This class provides a simplified API over the lower-level loader and reflection mapper. Most
 * users of the library should interact with this class instead of calling the underlying components
 * directly.
 */
public final class ConfigManager {

  /** Utility class; should never be instantiated. */
  private ConfigManager() {}

  /**
   * Loads a JSON configuration file into an intermediate {@link JsonTree}.
   *
   * @param jsonFile JSON file to load
   * @return Parsed JSON tree
   */
  public static JsonTree load(File jsonFile) {
    return JsonConfigLoader.load(jsonFile);
  }

  /**
   * Loads a JSON file and populates the provided configuration object.
   *
   * <p>Fields are matched by name using reflection.
   *
   * @param jsonFile JSON configuration file
   * @param config Configuration object to populate
   */
  public static void load(File jsonFile, Object config) {
    ReflectionMapper.populate(load(jsonFile), config);
  }
}
