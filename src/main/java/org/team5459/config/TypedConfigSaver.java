package org.team5459.config;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Persists an in-memory {@link ConfigDocument} back to disk.
 *
 * <p>Saving uses {@link ConfigJsonWriter} rather than Jackson serialization so the file contains
 * only the typed schema. This keeps round-trips stable even when node classes expose extra runtime
 * getters.
 */
public final class TypedConfigSaver {

  private TypedConfigSaver() {}

  /**
   * Writes a typed configuration document to disk.
   *
   * @param jsonFile Destination file
   * @param document Configuration document to save
   */
  public static void save(File jsonFile, ConfigDocument document) {
    try {
      ConfigJsonWriter.write(jsonFile, document.getRootEntries());
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Unable to save typed config file: " + jsonFile.getAbsolutePath(), exception);
    }
  }
}
