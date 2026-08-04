package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Step 6 — {@code config-cache.json} overlay and autosave of live values.
 *
 * <p>Layout may omit {@code properties.value}; the cache supplies tuned numbers across reboot.
 */
class Step6ConfigCacheTest {
  private static final Path EXAMPLE_CONFIG = Path.of("src/test/resources/robot-config.json");

  @Test
  void appliesCacheOverlayOnTopOfLayoutDefaults(@TempDir Path tempDirectory) throws Exception {
    Path cacheFile = tempDirectory.resolve("config-cache.json");
    Files.writeString(
        cacheFile,
        """
        {
          "/Config/Arm/PIDController": {
            "widgetType": "PIDController",
            "p": 0.9,
            "i": 0.05,
            "d": 0.01,
            "setpoint": 2.5
          },
          "/Config/Elevator/manual": {
            "widgetType": "Toggle Switch",
            "value": false
          }
        }
        """);

    ConfigDocument document = ConfigLoader.load(EXAMPLE_CONFIG.toFile());
    ConfigCache.apply(cacheFile.toFile(), document);

    assertEquals(0.9, document.getPIDController("Arm/PIDController").getP(), 1e-9);
    assertEquals(0.05, document.getPIDController("Arm/PIDController").getI(), 1e-9);
    assertEquals(0.01, document.getPIDController("Arm/PIDController").getD(), 1e-9);
    assertEquals(2.5, document.getPIDController("Arm/PIDController").getSetpoint(), 1e-9);
    assertFalse(document.getBoolean("Elevator/manual"));
    // Uncached layout default remains
    assertEquals(100.0, document.getDouble("Arm/operatorOffset"), 1e-9);
  }

  @Test
  void savesCurrentValuesAndReloadsThem(@TempDir Path tempDirectory) throws Exception {
    Path cacheFile = tempDirectory.resolve("config-cache.json");
    ConfigDocument document = ConfigLoader.load(EXAMPLE_CONFIG.toFile());
    document.getPIDController("Arm/PIDController").setP(0.42);
    document.getEntry("Elevator/manual");
    ((org.team5459.config.entries.BooleanEntry) document.getEntry("Elevator/manual"))
        .setValue(false);

    ConfigCache.save(cacheFile.toFile(), document);
    assertTrue(Files.exists(cacheFile));

    ConfigDocument reloaded = ConfigLoader.load(EXAMPLE_CONFIG.toFile());
    ConfigCache.apply(cacheFile.toFile(), reloaded);

    assertEquals(0.42, reloaded.getPIDController("Arm/PIDController").getP(), 1e-9);
    assertFalse(reloaded.getBoolean("Elevator/manual"));
  }

  @Test
  void loadsLayoutWithoutValueFieldsUsingCacheOnly(@TempDir Path tempDirectory) throws Exception {
    Path layoutFile = tempDirectory.resolve("robot-config.json");
    Path cacheFile = tempDirectory.resolve("config-cache.json");
    Files.writeString(
        layoutFile,
        """
        {
          "version": 1,
          "grid_size": 128,
          "tabs": [
            {
              "name": "Config",
              "grid_layout": {
                "layouts": [],
                "containers": [
                  {
                    "title": "Gain",
                    "type": "Text Display",
                    "properties": {
                      "topic": "/Config/Test/gain",
                      "data_type": "double"
                    }
                  },
                  {
                    "title": "Loop",
                    "type": "PIDController",
                    "properties": {
                      "topic": "/Config/Test/loop"
                    }
                  }
                ]
              }
            }
          ]
        }
        """);
    Files.writeString(
        cacheFile,
        """
        {
          "/Config/Test/gain": { "widgetType": "Text Display", "value": 7.5 },
          "/Config/Test/loop": {
            "widgetType": "PIDController",
            "p": 1.25,
            "i": 0.0,
            "d": 0.1,
            "setpoint": 0.0
          }
        }
        """);

    ConfigDocument document = ConfigLoader.load(layoutFile.toFile());
    assertEquals(0.0, document.getDouble("Test/gain"), 1e-9);
    assertEquals(0.0, document.getPIDController("Test/loop").getP(), 1e-9);

    ConfigCache.apply(cacheFile.toFile(), document);
    assertEquals(7.5, document.getDouble("Test/gain"), 1e-9);
    assertEquals(1.25, document.getPIDController("Test/loop").getP(), 1e-9);
  }
}
