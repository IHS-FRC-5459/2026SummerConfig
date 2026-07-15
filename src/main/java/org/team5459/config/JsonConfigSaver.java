package org.team5459.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

public final class JsonConfigSaver {

  private JsonConfigSaver() {}

  public static void save(File jsonFile, Object config) {
    try {
      new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(jsonFile, config);
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to save config file: " + jsonFile.getAbsolutePath(), e);
    }
  }
}
