package org.team5459.config.typed;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Loads typed configuration files into a {@link ConfigDocument}. */
public final class TypedConfigLoader {

  private TypedConfigLoader() {}

  /**
   * Reads a typed JSON configuration file.
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
}
