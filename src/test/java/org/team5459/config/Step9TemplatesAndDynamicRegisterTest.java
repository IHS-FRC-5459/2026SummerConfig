package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.networktables.NetworkTableInstance;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.team5459.config.types.DoubleNode;
import org.team5459.config.types.PIDControllerNode;

/** Templates folder + debug auto-register under /Config. */
class Step9TemplatesAndDynamicRegisterTest {
  private static final Path DEPLOY_CONFIG = Path.of("src/main/deploy/robot-config.json");

  @BeforeAll
  static void initHal() {
    HAL.initialize(500, 0);
  }

  @Test
  void deployConfigIncludesCuratedTemplatesFolder() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    assertNotNull(document.getNode("templates"));
    assertTrue(document.hasPath("templates/templateDouble"));
    assertTrue(document.hasPath("templates/templateInt"));
    assertTrue(document.hasPath("templates/templateBoolean"));
    assertTrue(document.hasPath("templates/templateString"));
    assertTrue(document.hasPath("templates/templatePIDController"));
    assertTrue(document.hasPath("templates/templateRotation2d"));
    assertTrue(document.hasPath("templates/templatePose2d"));
    assertTrue(document.hasPath("templates/templateTranslation2d"));
    assertTrue(document.hasPath("templates/templateSimpleMotorFeedforward"));
    assertTrue(document.hasPath("templates/templateArmFeedforward"));
    assertTrue(document.hasPath("templates/templateElevatorFeedforward"));
    assertTrue(document.getNode("templates/templatePIDController") instanceof PIDControllerNode);
  }

  @Test
  void insertLeafCreatesIntermediateFolders() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    assertTrue(document.insertLeaf("Intake/rollerSpeed", new DoubleNode(0.4)));
    assertEquals(0.4, document.getDouble("Intake/rollerSpeed"), 1e-9);
    assertFalse(document.insertLeaf("Intake/rollerSpeed", new DoubleNode(1.0)));
  }

  @Test
  void debugAutoRegisterAddsScalarUnderConfig(@TempDir Path tempDirectory) throws Exception {
    Path configFile = tempDirectory.resolve("robot-config.json");
    Path cacheFile = tempDirectory.resolve("config-cache.json");
    Path watchFile = tempDirectory.resolve("elastic-layout.json");
    Files.copy(DEPLOY_CONFIG, configFile);
    Files.writeString(watchFile, "{\"version\":1}");
    // Stabilize mtime so the promote watcher does not treat startup as a Save As.
    watchFile.toFile().setLastModified(System.currentTimeMillis() - 60_000);

    NetworkTableInstance.getDefault().getTable("Config").getEntry("DebugMode").setBoolean(true);

    ConfigManager manager =
        new ConfigManager(configFile.toFile(), cacheFile.toFile(), watchFile.toFile());
    try {
      assertTrue(manager.isDebugMode());

      String gainPath = "autoTuneGain_" + System.nanoTime();
      NetworkTableInstance.getDefault().getTable("Config").getEntry(gainPath).setDouble(1.25);
      NetworkTableInstance.getDefault().flush();
      NetworkTableInstance.getDefault().waitForListenerQueue(2.0);
      manager.periodic();
      NetworkTableInstance.getDefault().waitForListenerQueue(2.0);

      assertTrue(
          manager.getDocument().hasPath(gainPath), "Expected auto-register of /Config/" + gainPath);
      assertEquals(1.25, manager.getDocument().getDouble(gainPath), 1e-9);

      manager.promote();
      ConfigDocument committed = TypedConfigLoader.load(configFile.toFile());
      assertEquals(1.25, committed.getDouble(gainPath), 1e-9);
    } finally {
      manager.close();
    }
  }

  @Test
  void debugAutoRegisterCreatesPidWhenFieldsAppear(@TempDir Path tempDirectory) throws Exception {
    Path configFile = tempDirectory.resolve("robot-config.json");
    Path cacheFile = tempDirectory.resolve("config-cache.json");
    Path watchFile = tempDirectory.resolve("elastic-layout.json");
    Files.copy(DEPLOY_CONFIG, configFile);
    Files.writeString(watchFile, "{\"version\":1}");
    watchFile.toFile().setLastModified(System.currentTimeMillis() - 60_000);

    NetworkTableInstance.getDefault().getTable("Config").getEntry("DebugMode").setBoolean(true);

    ConfigManager manager =
        new ConfigManager(configFile.toFile(), cacheFile.toFile(), watchFile.toFile());
    try {
      assertTrue(manager.isDebugMode());

      String pidPath = "autoTunePid_" + System.nanoTime();
      var table = NetworkTableInstance.getDefault().getTable("Config").getSubTable(pidPath);
      table.getEntry("p").setDouble(1.1);
      table.getEntry("i").setDouble(0.2);
      table.getEntry("d").setDouble(0.3);
      table.getEntry(".type").setString("PIDController");
      NetworkTableInstance.getDefault().flush();
      NetworkTableInstance.getDefault().waitForListenerQueue(2.0);
      manager.periodic();
      NetworkTableInstance.getDefault().waitForListenerQueue(2.0);

      assertTrue(manager.getDocument().hasPath(pidPath));
      assertTrue(manager.getDocument().getNode(pidPath) instanceof PIDControllerNode);
      assertEquals(1.1, manager.getDocument().getPIDController(pidPath).getP(), 1e-9);
      assertEquals(
          "PIDController",
          NetworkTableInstance.getDefault()
              .getTable("Config")
              .getSubTable(pidPath)
              .getEntry(".type")
              .getString(""));
    } finally {
      manager.close();
    }
  }

  @Test
  void createPanelCreatesPidAndClearsForm(@TempDir Path tempDirectory) throws Exception {
    Path configFile = tempDirectory.resolve("robot-config.json");
    Path cacheFile = tempDirectory.resolve("config-cache.json");
    Path watchFile = tempDirectory.resolve("elastic-layout.json");
    Files.copy(DEPLOY_CONFIG, configFile);
    Files.writeString(watchFile, "{\"version\":1}");
    watchFile.toFile().setLastModified(System.currentTimeMillis() - 60_000);

    NetworkTableInstance.getDefault().getTable("Config").getEntry("DebugMode").setBoolean(true);

    ConfigManager manager =
        new ConfigManager(configFile.toFile(), cacheFile.toFile(), watchFile.toFile());
    try {
      String pidPath = "Intake/createdPid_" + System.nanoTime();
      var create =
          NetworkTableInstance.getDefault()
              .getTable(ConfigCreatePanel.TABLE)
              .getSubTable(ConfigCreatePanel.SUBTABLE);
      pulseCreate(create, "PIDController", pidPath);

      assertTrue(manager.getDocument().hasPath(pidPath));
      assertTrue(manager.getDocument().getNode(pidPath) instanceof PIDControllerNode);
      assertEquals("", create.getEntry(ConfigCreatePanel.PATH_ENTRY).getString("x"));
      assertFalse(create.getEntry(ConfigCreatePanel.GO_ENTRY).getBoolean(true));
      assertTrue(Files.exists(cacheFile));
    } finally {
      manager.close();
    }
  }

  @Test
  void createPanelCreatesDouble(@TempDir Path tempDirectory) throws Exception {
    Path configFile = tempDirectory.resolve("robot-config.json");
    Path cacheFile = tempDirectory.resolve("config-cache.json");
    Path watchFile = tempDirectory.resolve("elastic-layout.json");
    Files.copy(DEPLOY_CONFIG, configFile);
    Files.writeString(watchFile, "{\"version\":1}");
    watchFile.toFile().setLastModified(System.currentTimeMillis() - 60_000);

    NetworkTableInstance.getDefault().getTable("Config").getEntry("DebugMode").setBoolean(true);

    ConfigManager manager =
        new ConfigManager(configFile.toFile(), cacheFile.toFile(), watchFile.toFile());
    try {
      String path = "Intake/roller_" + System.nanoTime();
      var create =
          NetworkTableInstance.getDefault()
              .getTable(ConfigCreatePanel.TABLE)
              .getSubTable(ConfigCreatePanel.SUBTABLE);
      pulseCreate(create, "Double", path);

      assertTrue(manager.getDocument().hasPath(path), "Expected created /Config/" + path);
      assertEquals(0.0, manager.getDocument().getDouble(path), 1e-9);
    } finally {
      manager.close();
    }
  }

  @Test
  void createPanelRejectsExistingPathWithoutClearingPath(@TempDir Path tempDirectory)
      throws Exception {
    Path configFile = tempDirectory.resolve("robot-config.json");
    Path cacheFile = tempDirectory.resolve("config-cache.json");
    Path watchFile = tempDirectory.resolve("elastic-layout.json");
    Files.copy(DEPLOY_CONFIG, configFile);
    Files.writeString(watchFile, "{\"version\":1}");
    watchFile.toFile().setLastModified(System.currentTimeMillis() - 60_000);

    NetworkTableInstance.getDefault().getTable("Config").getEntry("DebugMode").setBoolean(true);

    ConfigManager manager =
        new ConfigManager(configFile.toFile(), cacheFile.toFile(), watchFile.toFile());
    try {
      var create =
          NetworkTableInstance.getDefault()
              .getTable(ConfigCreatePanel.TABLE)
              .getSubTable(ConfigCreatePanel.SUBTABLE);
      pulseCreate(create, "Double", "Arm/operatorOffset");

      assertEquals(
          "Arm/operatorOffset", create.getEntry(ConfigCreatePanel.PATH_ENTRY).getString(""));
      assertFalse(create.getEntry(ConfigCreatePanel.GO_ENTRY).getBoolean(true));
    } finally {
      manager.close();
    }
  }

  private static void pulseCreate(
      edu.wpi.first.networktables.NetworkTable create, String type, String path) {
    create.getEntry(ConfigCreatePanel.GO_ENTRY).setBoolean(false);
    NetworkTableInstance.getDefault().flush();
    NetworkTableInstance.getDefault().waitForListenerQueue(1.0);

    create.getSubTable(ConfigCreatePanel.TYPE_SUBTABLE).getEntry("selected").setString(type);
    create.getEntry(ConfigCreatePanel.PATH_ENTRY).setString(path);
    NetworkTableInstance.getDefault().flush();
    NetworkTableInstance.getDefault().waitForListenerQueue(0.5);

    create.getEntry(ConfigCreatePanel.GO_ENTRY).setBoolean(true);
    NetworkTableInstance.getDefault().flush();
    NetworkTableInstance.getDefault().waitForListenerQueue(2.0);
  }
}
