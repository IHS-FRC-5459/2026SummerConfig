package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.team5459.config.typed.ConfigDocument;
import org.team5459.config.typed.TypedConfigLoader;
import org.team5459.config.typed.TypedConfigSaver;
import org.team5459.config.typed.TypedNetworkTableSync;

/** Verifies step 5: remote NetworkTables edits update the document and save clean JSON. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Step5NetworkTableUpdateTest {
  private static final int SERVER_PORT = 1760;
  private static final int SERVER_NT4_PORT = 5860;
  private static final Path EXAMPLE_CONFIG =
      Path.of("src/test/resources/typed-example-config.json");

  private final NetworkTableInstance robotInstance = NetworkTableInstance.getDefault();
  private NetworkTableInstance dashboardInstance;
  private NetworkTableListener[] listeners;

  @BeforeAll
  void startNetworkTables() {
    robotInstance.stopClient();
    robotInstance.stopServer();
    robotInstance.startServer("", "", SERVER_PORT, SERVER_NT4_PORT);
    dashboardInstance = NetworkTableInstance.create();
    dashboardInstance.setServer("127.0.0.1", SERVER_NT4_PORT);
    dashboardInstance.startClient4("Step5NetworkTableUpdateTest");
  }

  @AfterAll
  void stopNetworkTables() {
    closeListeners();
    if (dashboardInstance != null) {
      dashboardInstance.stopClient();
      dashboardInstance.close();
      dashboardInstance = null;
    }
    robotInstance.stopServer();
    robotInstance.stopClient();
  }

  @AfterEach
  void closeTestListeners() {
    closeListeners();
  }

  @Test
  void appliesRemoteDashboardUpdateToTypedDocument() throws Exception {
    ConfigDocument document = TypedConfigLoader.load(EXAMPLE_CONFIG.toFile());
    CountDownLatch updateReceived = new CountDownLatch(1);
    listeners = TypedNetworkTableSync.listen(document, updateReceived::countDown);
    TypedNetworkTableSync.publish(document);

    assertTrue(waitForDashboardConnection(), "Dashboard client did not connect to NetworkTables");
    assertTrue(
        waitForPublishedPidValue(0.1),
        "Dashboard client did not receive published PID values from the robot");
    dashboardInstance
        .getTable("Config")
        .getSubTable("Arm")
        .getSubTable("PIDController")
        .getEntry("p")
        .setDouble(0.5);
    dashboardInstance.flush();
    robotInstance.waitForListenerQueue(2.0);

    assertTrue(updateReceived.await(5, TimeUnit.SECONDS), "Remote update was not received");
    assertEquals(0.5, document.getDouble("Arm/PIDController/p"));
    assertEquals(0.5, document.getPIDController("Arm/PIDController").getP());
  }

  @Test
  void savesOnlyTypeAndValueFields(@TempDir Path tempDirectory) throws Exception {
    ConfigDocument document = TypedConfigLoader.load(EXAMPLE_CONFIG.toFile());
    Path savedFile = tempDirectory.resolve("saved-config.json");

    TypedConfigSaver.save(savedFile.toFile(), document);

    String json = Files.readString(savedFile);
    assertFalse(
        json.contains("controller"), "Saved JSON must not include live PIDController state");
    assertFalse(json.contains("childEntries"), "Saved JSON must not include path traversal maps");
    assertFalse(
        json.contains("\"rotation\""), "Saved JSON must not include built Rotation2d values");

    ConfigDocument reloaded = TypedConfigLoader.load(savedFile.toFile());
    assertEquals(
        document.getPIDController("Arm/PIDController").getP(),
        reloaded.getPIDController("Arm/PIDController").getP());
    assertEquals(document.getDouble("Arm/Rotation/deg"), reloaded.getDouble("Arm/Rotation/deg"));
  }

  private void closeListeners() {
    if (listeners != null) {
      for (NetworkTableListener listener : listeners) {
        listener.close();
      }
      listeners = null;
    }
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

  private boolean waitForPublishedPidValue(double expectedValue) throws InterruptedException {
    for (int attempt = 0; attempt < 40; attempt++) {
      double publishedValue =
          dashboardInstance
              .getTable("Config")
              .getSubTable("Arm")
              .getSubTable("PIDController")
              .getEntry("p")
              .getDouble(Double.NaN);
      if (Double.compare(publishedValue, expectedValue) == 0) {
        return true;
      }
      Thread.sleep(50);
    }
    return false;
  }
}
