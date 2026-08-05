package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import edu.wpi.first.networktables.NetworkTableInstance;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.team5459.config.types.DoubleNode;
import org.team5459.config.types.PIDControllerNode;

/**
 * Step 10 — Elastic multi-topic widget discovery via Sendable-style {@code .type} topics.
 *
 * @see <a
 *     href="https://frc-elastic.gitbook.io/docs/additional-features-and-references/widgets-list-and-properties-reference">Elastic
 *     Widgets List</a>
 */
class Step10ElasticWidgetTypesTest {
  private static final Path DEPLOY_CONFIG = Path.of("src/main/deploy/robot-config.json");

  @Test
  void mapsPidControllerNodeToElasticType() {
    assertEquals("PIDController", ConfigElasticTypes.elasticTypeFor(new PIDControllerNode(null)));
    assertNull(ConfigElasticTypes.elasticTypeFor(new DoubleNode(1.0)));
  }

  @Test
  void publishesTypeAndSetpointForPidTables() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    TypedNetworkTableSync.publish(document);

    var armPid =
        NetworkTableInstance.getDefault()
            .getTable("Config")
            .getSubTable("Arm")
            .getSubTable("PIDController");
    var templatePid =
        NetworkTableInstance.getDefault()
            .getTable("Config")
            .getSubTable("templates")
            .getSubTable("templatePIDController");

    assertEquals("PIDController", armPid.getEntry(".type").getString(""));
    assertEquals("PIDController", templatePid.getEntry(".type").getString(""));
    assertEquals(0.0, armPid.getEntry("setpoint").getDouble(-1.0));
  }
}
