package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.wpi.first.math.geometry.Rotation2d;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Step 2 — load typed JSON into a {@link ConfigDocument}.
 *
 * <p>Uses {@code src/test/resources/robot-config.json} to verify nested folders, composite PID
 * fields, geometry nodes, and array values deserialize to the expected runtime numbers.
 */
class Step2TypedConfigLoaderTest {
  private static final Path EXAMPLE_CONFIG = Path.of("src/test/resources/robot-config.json");

  @Test
  void loadsNestedTypedValuesFromJsonFile() {
    ConfigDocument document = TypedConfigLoader.load(EXAMPLE_CONFIG.toFile());

    assertEquals(0.1, document.getDouble("Arm/PIDController/p"));
    assertEquals(0.01, document.getDouble("Arm/PIDController/i"));
    assertEquals(0.001, document.getDouble("Arm/PIDController/d"));
    assertEquals(0.1, document.getPIDController("Arm/PIDController").getP());
    assertEquals(0.01, document.getPIDController("Arm/PIDController").getI());
    assertEquals(0.001, document.getPIDController("Arm/PIDController").getD());

    assertEquals(
        Rotation2d.fromDegrees(100.0).getRadians(),
        document.getRotation2d("Arm/Rotation").getRadians(),
        1e-9);
    assertEquals(100.0, document.getDouble("Arm/Rotation/deg"));

    assertArrayEquals(new double[] {0.0, 20.0, 45.0}, document.getDoubleArray("Arm/Setpoints"));

    assertEquals(0.2, document.getPIDController("Elevator/PIDController").getP());
  }
}
