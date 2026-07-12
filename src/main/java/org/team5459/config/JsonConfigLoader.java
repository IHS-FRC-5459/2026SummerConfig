package org.team5459.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

public final class JsonConfigLoader {

  private JsonConfigLoader() {}

  public static JsonTree load(File jsonFile) {
    try {
      return new JsonTree(new ObjectMapper().readTree(jsonFile));
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Unable to read config file: " + jsonFile.getAbsolutePath(), e);
    }
  }
}
