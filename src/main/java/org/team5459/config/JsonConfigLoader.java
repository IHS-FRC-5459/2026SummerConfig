package org.team5459.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Reads configuration data from JSON files.
 *
 * <p>This class is intentionally only responsible for parsing JSON. Converting JSON into
 * configuration objects is handled separately by {@link ReflectionMapper}.
 */
public final class JsonConfigLoader {

  /** Utility class; should never be instantiated. */
  private JsonConfigLoader() {}

  /**
   * Reads a JSON file into a {@link JsonTree}.
   *
   * @param jsonFile JSON file to load
   * @return Parsed JSON tree
   * @throws UncheckedIOException if the file cannot be read
   */
  public static JsonTree load(File jsonFile) {
    try {
      return new JsonTree(new ObjectMapper().readTree(jsonFile));
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Unable to read config file: " + jsonFile.getAbsolutePath(), e);
    }
  }
}
