package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies step 5: remote dashboard updates change the config API and can be saved to JSON. */
class Step5NetworkTableUpdateTest {
  private static final int SERVER_PORT = 1740;
  private static final int SERVER_NT4_PORT = 5810;

  @TempDir Path tempDirectory;

  private final NetworkTableInstance robotInstance = NetworkTableInstance.getDefault();
  private NetworkTableInstance dashboardInstance;
  private NetworkTableListener[] listeners;

  static class TunableConfig {
    public double speed = 1.0;
  }

  @BeforeEach
  void startNetworkTables() {
    robotInstance.stopClient();
    robotInstance.startServer("", "", SERVER_PORT);
    dashboardInstance = NetworkTableInstance.create();
    dashboardInstance.setServer("127.0.0.1", SERVER_NT4_PORT);
    dashboardInstance.startClient4("Step5NetworkTableUpdateTest");
  }

  @AfterEach
  void stopNetworkTables() {
    if (listeners != null) {
      for (NetworkTableListener listener : listeners) {
        listener.close();
      }
    }
    if (dashboardInstance != null) {
      dashboardInstance.stopClient();
      dashboardInstance.close();
    }
    robotInstance.stopServer();
  }

  @Test
  void appliesRemoteDashboardUpdateAndSavesUpdatedConfiguration() throws Exception {
    TunableConfig config = new TunableConfig();
    Path configFile = tempDirectory.resolve("tunable-config.json");
    CountDownLatch updateReceived = new CountDownLatch(1);
    listeners =
        NetworkTableSync.listen(
            config,
            () -> {
              JsonConfigSaver.save(configFile.toFile(), config);
              updateReceived.countDown();
            });

    assertTrue(waitForDashboardConnection(), "Dashboard client did not connect to NetworkTables");
    dashboardInstance.getTable("Config").getEntry("speed").setDouble(9.5);

    assertTrue(updateReceived.await(2, TimeUnit.SECONDS), "Remote update was not received");
    assertEquals(9.5, config.speed);
    assertEquals(9.5, ConfigManager.load(configFile.toFile()).getRoot().get("speed").asDouble());
  }

  private boolean waitForDashboardConnection() throws InterruptedException {
    for (int attempt = 0; attempt < 40; attempt++) {
      if (dashboardInstance.isConnected()) {
        return true;
      }
      Thread.sleep(50);
    }
    return false;
  }
}
