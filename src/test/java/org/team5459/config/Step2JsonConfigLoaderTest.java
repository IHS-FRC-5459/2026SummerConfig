package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies step 2: JSON configuration files can be read into a tree. */
class Step2JsonConfigLoaderTest {
  @TempDir Path tempDirectory;

  @Test
  void loadsNumericValuesFromJsonFile() throws Exception {
    Path configFile = tempDirectory.resolve("robot-config.json");
    Files.writeString(configFile, "{\"speed\": 7.25, \"id\": 42}");

    JsonTree tree = JsonConfigLoader.load(configFile.toFile());

    assertEquals(7.25, tree.getRoot().get("speed").asDouble());
    assertEquals(42, tree.getRoot().get("id").asInt());
  }
}
