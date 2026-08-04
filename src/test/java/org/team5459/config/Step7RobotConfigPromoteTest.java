package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Step 7 — promote Elastic Save As layout + live/cache values into {@code robot-config.json}. */
class Step7RobotConfigPromoteTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void writesLayoutWithMergedValuesIntoRobotConfig(@TempDir Path tempDirectory) throws Exception {
    Path elasticLayout = tempDirectory.resolve("elastic-layout.json");
    Path robotConfig = tempDirectory.resolve("robot-config.json");
    Path cache = tempDirectory.resolve("config-cache.json");

    Files.writeString(
        elasticLayout,
        """
        {
          "version": 1.0,
          "grid_size": 128,
          "tabs": [
            {
              "name": "Teleoperated",
              "grid_layout": {
                "layouts": [],
                "containers": [
                  {
                    "title": "operatorOffset",
                    "type": "Text Display",
                    "x": 0,
                    "y": 0,
                    "width": 128,
                    "height": 128,
                    "properties": {
                      "topic": "/Config/Arm/operatorOffset",
                      "data_type": "double",
                      "period": 0.06
                    }
                  },
                  {
                    "title": "Arm PID",
                    "type": "PIDController",
                    "x": 128,
                    "y": 0,
                    "width": 256,
                    "height": 256,
                    "properties": {
                      "topic": "/Config/Arm/PIDController",
                      "period": 0.06
                    }
                  }
                ]
              }
            }
          ]
        }
        """);
    Files.writeString(
        cache,
        """
        {
          "/Config/Arm/operatorOffset": { "widgetType": "Text Display", "value": 112.0 },
          "/Config/Arm/PIDController": {
            "widgetType": "PIDController",
            "p": 0.5,
            "i": 0.02,
            "d": 0.01,
            "setpoint": 1.0
          }
        }
        """);

    try (var ignored =
        new AutoCloseable() {
          final ConfigManager manager = new ConfigManager(robotConfig.toFile(), cache.toFile());

          @Override
          public void close() {
            manager.close();
          }
        }) {
      // ConfigManager constructor does the promote.
    }

    assertTrue(Files.exists(robotConfig));
    JsonNode written = MAPPER.readTree(robotConfig.toFile());
    JsonNode containers = written.at("/tabs/0/grid_layout/containers");
    assertEquals(112.0, containers.get(0).at("/properties/value").asDouble(), 1e-9);
    assertEquals(0.5, containers.get(1).at("/properties/value/p").asDouble(), 1e-9);
    assertEquals(0.02, containers.get(1).at("/properties/value/i").asDouble(), 1e-9);
    assertEquals("PIDController", containers.get(1).get("type").asText());
  }

  @Test
  void loadsWidgetsNestedInListLayout(@TempDir Path tempDirectory) throws Exception {
    Path layoutFile = tempDirectory.resolve("layout.json");
    Files.writeString(
        layoutFile,
        """
        {
          "version": 1.0,
          "grid_size": 128,
          "tabs": [
            {
              "name": "Teleoperated",
              "grid_layout": {
                "layouts": [
                  {
                    "title": "Arm",
                    "type": "List Layout",
                    "properties": { "label_position": "TOP" },
                    "children": [
                      {
                        "title": "PIDController",
                        "type": "PIDController",
                        "properties": {
                          "topic": "/Config/Arm/PIDController",
                          "value": { "p": 0.2, "i": 0.0, "d": 0.01, "setpoint": 0.0 }
                        }
                      }
                    ]
                  }
                ],
                "containers": []
              }
            }
          ]
        }
        """);

    ConfigDocument document = ConfigLoader.load(layoutFile.toFile());
    assertEquals(0.2, document.getPIDController("Arm/PIDController").getP(), 1e-9);
  }

  @Test
  void resolveLayoutSourcePrefersElasticLayoutSibling(@TempDir Path tempDirectory)
      throws Exception {
    Path robotConfig = tempDirectory.resolve("robot-config.json");
    Path elastic = tempDirectory.resolve("elastic-layout.json");
    Files.writeString(robotConfig, "{\"version\":1,\"tabs\":[]}");
    Files.writeString(elastic, "{\"version\":1.0,\"tabs\":[]}");

    assertEquals(
        elastic.toFile().getAbsolutePath(),
        ConfigManager.resolveLayoutSource(robotConfig.toFile()).getAbsolutePath());
  }
}
