package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies step 3: loaded configuration values are usable through a Java config object. */
class Step3ConfigManagerTest {
  @TempDir Path tempDirectory;

  static class DriveConfig {
    public double speed = 0.0;
    public int id = 0;
  }

  @Test
  void populatesConfigObjectWithValuesFromFile() throws Exception {
    Path configFile = tempDirectory.resolve("drive-config.json");
    Files.writeString(configFile, "{\"speed\": 6.5, \"id\": 12}");
    DriveConfig config = new DriveConfig();

    ConfigManager.load(configFile.toFile(), config);

    assertEquals(6.5, config.speed);
    assertEquals(12, config.id);
  }
}
