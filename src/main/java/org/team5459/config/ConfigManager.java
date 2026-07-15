package org.team5459.config;

import java.io.File;

public final class ConfigManager {

  private ConfigManager() {}

  public static JsonTree load(File jsonFile) {
    return JsonConfigLoader.load(jsonFile);
  }

  public static void load(File jsonFile, Object config) {
    ReflectionMapper.populate(load(jsonFile), config);
  }
}
