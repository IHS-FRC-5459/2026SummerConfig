package org.team5459.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Writes configuration objects to JSON files.
 *
 * <p>The output is formatted using Jackson's default pretty printer so it remains easy for humans
 * to edit.
 */
public final class JsonConfigSaver {

  /** Utility class; should never be instantiated. */
  private JsonConfigSaver() {}

  /**
   * Saves a configuration object to disk.
   *
   * @param jsonFile Destination file
   * @param config Configuration object to serialize
   * @throws UncheckedIOException if the file cannot be written
   */
  public static void save(File jsonFile, Object config) {
    try {
      new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(jsonFile, config);
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Unable to save config file: " + jsonFile.getAbsolutePath(), e);
    }
  }
}
