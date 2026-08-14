package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.networktables.NetworkTableInstance;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.team5459.config.types.DoubleNode;
import org.team5459.config.types.FolderNode;
import org.team5459.config.types.PIDControllerNode;

/**
 * Step 10 — Elastic multi-topic widget discovery via Sendable-style {@code .type} topics.
 *
 * @see <a
 *     href="https://frc-elastic.gitbook.io/docs/additional-features-and-references/widgets-list-and-properties-reference">Elastic
 *     Widgets List</a>
 */
class Step10ElasticWidgetTypesTest {
  private static final Path TEST_CONFIG = Path.of("src/test/resources/robot-config.json");

  @Test
  void mapsPidControllerNodeToElasticType() {
    assertEquals("PIDController", ConfigElasticTypes.elasticTypeFor(new PIDControllerNode(null)));
    assertEquals("Folder", ConfigElasticTypes.elasticTypeFor(new FolderNode(null)));
    assertNull(ConfigElasticTypes.elasticTypeFor(new DoubleNode(1.0)));
  }

  @Test
  void publishesTypeAndSetpointForPidTables() {
    ConfigDocument document = TypedConfigLoader.load(TEST_CONFIG.toFile());
    TypedNetworkTableSync.publish(document);

    var armPid =
        NetworkTableInstance.getDefault()
            .getTable("Config")
            .getSubTable("Arm")
            .getSubTable("PIDController");
    assertEquals("PIDController", armPid.getEntry(".type").getString(""));
    assertEquals(0.0, armPid.getEntry("setpoint").getDouble(-1.0));
  }

  @Test
  void publishesTypeForEmptyFolder() {
    ConfigDocument document = TypedConfigLoader.load(TEST_CONFIG.toFile());
    assertTrue(ConfigCreateHelper.create(document, ConfigCreateHelper.FOLDER_TYPE, "Empty"));
    TypedNetworkTableSync.publish(document);

    assertEquals(
        "Folder",
        NetworkTableInstance.getDefault()
            .getTable("Config")
            .getSubTable("Empty")
            .getEntry(".type")
            .getString(""));
    assertEquals(
        "Folder",
        NetworkTableInstance.getDefault()
            .getTable("Config")
            .getSubTable("Arm")
            .getEntry(".type")
            .getString(""));
  }
}
