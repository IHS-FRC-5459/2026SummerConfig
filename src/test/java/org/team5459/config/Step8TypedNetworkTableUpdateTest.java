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
import org.team5459.config.typed.ConfigDocument;
import org.team5459.config.typed.TypedConfigLoader;
import org.team5459.config.typed.TypedNetworkTableSync;

/** Verifies step 8: remote NetworkTables edits update the typed config document. */
class Step8TypedNetworkTableUpdateTest {
  private static final int SERVER_PORT = 1740;
  private static final int SERVER_NT4_PORT = 5810;
  private static final Path EXAMPLE_CONFIG =
      Path.of("src/test/resources/typed-example-config.json");

  private final NetworkTableInstance robotInstance = NetworkTableInstance.getDefault();
  private NetworkTableInstance dashboardInstance;
  private NetworkTableListener[] listeners;

  @BeforeEach
  void startNetworkTables() {
    robotInstance.stopClient();
    robotInstance.startServer("", "", SERVER_PORT);
    dashboardInstance = NetworkTableInstance.create();
    dashboardInstance.setServer("127.0.0.1", SERVER_NT4_PORT);
    dashboardInstance.startClient4("Step8TypedNetworkTableUpdateTest");
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
  void appliesRemoteDashboardUpdateToTypedDocument() throws Exception {
    ConfigDocument document = TypedConfigLoader.load(EXAMPLE_CONFIG.toFile());
    TypedNetworkTableSync.publish(document);
    CountDownLatch updateReceived = new CountDownLatch(1);
    listeners = TypedNetworkTableSync.listen(document, updateReceived::countDown);

    assertTrue(waitForDashboardConnection(), "Dashboard client did not connect to NetworkTables");
    dashboardInstance
        .getTable("Config")
        .getSubTable("Arm")
        .getSubTable("PIDController")
        .getEntry("p")
        .setDouble(0.5);

    assertTrue(updateReceived.await(2, TimeUnit.SECONDS), "Remote update was not received");
    assertEquals(0.5, document.getDouble("Arm/PIDController/p"));
    assertEquals(0.5, document.getPIDController("Arm/PIDController").getP());
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
