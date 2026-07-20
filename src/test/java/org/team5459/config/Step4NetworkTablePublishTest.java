package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.wpi.first.networktables.NetworkTableInstance;
import org.junit.jupiter.api.Test;

/** Verifies step 4: configuration values are published for dashboards under the Config table. */
class Step4NetworkTablePublishTest {
  static class DashboardConfig {
    public double speed = 4.75;
    public int id = 8;
  }

  @Test
  void publishesEverySupportedFieldToConfigTable() {
    NetworkTableSync.publish(new DashboardConfig());

    var table = NetworkTableInstance.getDefault().getTable("Config");
    assertEquals(4.75, table.getEntry("speed").getDouble(0.0));
    assertEquals(8, table.getEntry("id").getInteger(0));
  }
}
