package org.team5459.config.typed;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/** Saves typed configuration documents back to JSON files. */
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
