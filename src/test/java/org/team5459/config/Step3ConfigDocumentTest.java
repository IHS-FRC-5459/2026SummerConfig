package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.util.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.team5459.config.types.ColorNode;
import org.team5459.config.types.DoubleNode;
import org.team5459.config.types.PIDControllerNode;
import org.team5459.config.types.Rotation2dNode;

/**
 * Step 3 — read loaded values through {@link ConfigDocument} getters and direct node access.
 *
 * <p>Covers default/warn behavior for bad paths, live {@link PIDController} identity, path
 * resolution to concrete node types, and representative WPILib composites loaded from inline JSON.
 */
class Step3ConfigDocumentTest {
  private static final Path EXAMPLE_CONFIG = Path.of("src/test/resources/robot-config.json");

  @Test
  void returnsDefaultsAndWarnsWhenPathOrTypeIsWrong() {
    ConfigDocument document = TypedConfigLoader.load(EXAMPLE_CONFIG.toFile());

    assertEquals(0.0, document.getDouble("Arm/Missing"));
    assertEquals(0.0, document.getInt("Arm/PIDController/p"));
    assertEquals(0, document.getIntArray("Arm/Setpoints").length);
  }

  @Test
  void returnsSameLivePidControllerInstance() {
    ConfigDocument document = TypedConfigLoader.load(EXAMPLE_CONFIG.toFile());

    PIDController first = document.getPIDController("Arm/PIDController");
    PIDController second = document.getPIDController("Arm/PIDController");
    assertSame(first, second);

    PIDControllerNode pidNode = (PIDControllerNode) document.getNode("Arm/PIDController");
    DoubleNode pNode = (DoubleNode) document.getNode("Arm/PIDController/p");
    pNode.setValue(0.5);
    pidNode.syncController();

    assertEquals(0.5, first.getP());
    assertEquals(0.5, document.getDouble("Arm/PIDController/p"));
  }

  @Test
  void resolvesNodesByPath() {
    ConfigDocument document = TypedConfigLoader.load(EXAMPLE_CONFIG.toFile());

    assertInstanceOf(PIDControllerNode.class, document.getNode("Arm/PIDController"));
    assertInstanceOf(DoubleNode.class, document.getNode("Arm/PIDController/p"));
    assertInstanceOf(Rotation2dNode.class, document.getNode("Arm/Rotation"));
  }

  @Test
  void loadsRepresentativeWpilibTypesFromInlineJson(@TempDir Path tempDirectory) throws Exception {
    Path configFile = tempDirectory.resolve("wpilib-types-config.json");
    Files.writeString(
        configFile,
        """
        {
          "Robot": {
            "type": "folder",
            "value": {
              "StartPose": {
                "type": "Pose2d",
                "value": {
                  "x": { "type": "double", "value": 3.0 },
                  "y": { "type": "double", "value": 1.5 },
                  "deg": { "type": "double", "value": 90.0 }
                }
              },
              "Feedforward": {
                "type": "ArmFeedforward",
                "value": {
                  "ks": { "type": "double", "value": 0.05 },
                  "kg": { "type": "double", "value": 0.2 },
                  "kv": { "type": "double", "value": 1.2 },
                  "ka": { "type": "double", "value": 0.01 }
                }
              },
              "LEDColor": {
                "type": "Color",
                "value": {
                  "red": { "type": "int", "value": 255 },
                  "green": { "type": "int", "value": 128 },
                  "blue": { "type": "int", "value": 0 }
                }
              },
              "Field": {
                "type": "AprilTagFieldLayout",
                "value": {
                  "field": { "type": "String", "value": "kDefaultField" }
                }
              }
            }
          }
        }
        """);

    ConfigDocument document = TypedConfigLoader.load(configFile.toFile());

    Pose2d startPose = document.getPose2d("Robot/StartPose");
    assertEquals(3.0, startPose.getX(), 1e-9);
    assertEquals(1.5, startPose.getY(), 1e-9);
    assertEquals(
        Rotation2d.fromDegrees(90.0).getRadians(), startPose.getRotation().getRadians(), 1e-9);

    ArmFeedforward feedforward = document.getArmFeedforward("Robot/Feedforward");
    assertEquals(0.05, feedforward.getKs(), 1e-9);
    assertEquals(0.2, feedforward.getKg(), 1e-9);
    assertEquals(1.2, feedforward.getKv(), 1e-9);
    assertEquals(0.01, feedforward.getKa(), 1e-9);

    Color color = document.getColor("Robot/LEDColor");
    assertEquals(new Color(255, 128, 0), color);

    AprilTagFieldLayout fieldLayout = document.getAprilTagFieldLayout("Robot/Field");
    assertTrue(fieldLayout.getFieldLength() > 0.0);
    assertTrue(fieldLayout.getFieldWidth() > 0.0);
    assertInstanceOf(ColorNode.class, document.getNode("Robot/LEDColor"));
  }
}
