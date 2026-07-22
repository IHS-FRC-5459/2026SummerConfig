package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.wpi.first.networktables.NetworkTableInstance;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.team5459.config.typed.ConfigDocument;
import org.team5459.config.typed.TypedConfigLoader;
import org.team5459.config.typed.TypedNetworkTableSync;

/** Verifies step 4: typed configuration values publish to nested NetworkTables paths. */
class Step4NetworkTablePublishTest {
  private static final Path EXAMPLE_CONFIG =
      Path.of("src/test/resources/typed-example-config.json");

  @Test
  void publishesNestedTypedValuesToConfigTable() {
    ConfigDocument document = TypedConfigLoader.load(EXAMPLE_CONFIG.toFile());
    TypedNetworkTableSync.publish(document);

    var armTable = NetworkTableInstance.getDefault().getTable("Config").getSubTable("Arm");
    var pidTable = armTable.getSubTable("PIDController");

    assertEquals(0.1, pidTable.getEntry("p").getDouble(0.0));
    assertEquals(0.01, pidTable.getEntry("i").getDouble(0.0));
    assertEquals(0.001, pidTable.getEntry("d").getDouble(0.0));
    assertEquals(100.0, armTable.getSubTable("Rotation").getEntry("deg").getDouble(0.0));
    assertArrayEquals(
        new double[] {0.0, 20.0, 45.0},
        armTable.getEntry("Setpoints").getDoubleArray(new double[0]));
  }
}
