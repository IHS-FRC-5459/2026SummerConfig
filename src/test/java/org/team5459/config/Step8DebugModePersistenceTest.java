package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.team5459.config.types.DoubleNode;

/** Debug-mode cache overlay, promote watcher, and Save As content-change detection. */
class Step8DebugModePersistenceTest {
  private static final Path EXAMPLE_CONFIG = Path.of("src/test/resources/robot-config.json");

  @BeforeAll
  static void initHal() {
    HAL.initialize(500, 0);
  }

  @BeforeEach
  void resetDebugModeWidget() {
    DriverStationSim.setFmsAttached(false);
    DriverStationSim.notifyNewData();
    debugModeEntry().setBoolean(true);
  }

  @Test
  void overlayAppliesCachedScalarValuesOntoDefaults(@TempDir Path tempDirectory) throws Exception {
    ConfigDocument defaults = TypedConfigLoader.load(EXAMPLE_CONFIG.toFile());
    ConfigDocument live = TypedConfigLoader.load(EXAMPLE_CONFIG.toFile());
    ((DoubleNode) live.getNode("Arm/PIDController/p")).setValue(0.9);

    Path cacheFile = tempDirectory.resolve("config-cache.json");
    TypedConfigSaver.save(cacheFile.toFile(), live);

    ConfigDocument reloaded = TypedConfigLoader.load(EXAMPLE_CONFIG.toFile());
    assertEquals(0.1, reloaded.getDouble("Arm/PIDController/p"), 1e-9);

    TypedConfigOverlay.applyFile(cacheFile.toFile(), reloaded);
    assertEquals(0.9, reloaded.getDouble("Arm/PIDController/p"), 1e-9);
    assertEquals(0.9, reloaded.getPIDController("Arm/PIDController").getP(), 1e-9);
    assertEquals(defaults.getDouble("Arm/Rotation/deg"), reloaded.getDouble("Arm/Rotation/deg"));
  }

  @Test
  void promoteWatcherFiresOnlyWhenFileContentChanges(@TempDir Path tempDirectory) throws Exception {
    Path watched = tempDirectory.resolve("elastic-layout.json");
    Files.writeString(watched, "{\"version\":1,\"tabs\":[]}");

    AtomicInteger promotions = new AtomicInteger();
    ConfigPromoteWatcher watcher =
        new ConfigPromoteWatcher(watched.toFile(), promotions::incrementAndGet, 100);

    watcher.poll();
    assertEquals(0, promotions.get(), "Initial snapshot must not promote");

    Thread.sleep(110);
    watcher.poll();
    assertEquals(0, promotions.get(), "Unchanged content must not promote");

    Files.writeString(watched, "{\"version\":1,\"tabs\":[{\"name\":\"Teleop\"}]}");
    watched.toFile().setLastModified(System.currentTimeMillis() + 1000);

    Thread.sleep(110);
    watcher.poll();
    assertEquals(1, promotions.get(), "Content change must promote once");

    Thread.sleep(110);
    watcher.poll();
    assertEquals(1, promotions.get(), "Repeat poll without change must not re-promote");
  }

  @Test
  void configManagerDebugGettersUseLiveDocumentAndPromoteWritesConfig(@TempDir Path tempDirectory)
      throws Exception {
    Path configFile = tempDirectory.resolve("robot-config.json");
    Path cacheFile = tempDirectory.resolve("config-cache.json");
    Path watchFile = tempDirectory.resolve("elastic-layout.json");
    Files.copy(EXAMPLE_CONFIG, configFile);
    Files.writeString(watchFile, "{\"version\":1}");

    debugModeEntry().setBoolean(true);

    ConfigManager manager =
        new ConfigManager(configFile.toFile(), cacheFile.toFile(), watchFile.toFile());
    try {
      assertTrue(
          manager.isDebugMode(),
          () ->
              "expected debug mode, FMS="
                  + DriverStation.isFMSAttached()
                  + " DebugMode="
                  + debugModeEntry().getBoolean(true));

      ((DoubleNode) manager.getDocument().getNode("Arm/PIDController/p")).setValue(0.55);
      // Promote pulls NT → document; push the live doc so the pull keeps 0.55.
      TypedNetworkTableSync.publish(manager.getDocument());
      manager.promote();

      ConfigDocument committed = TypedConfigLoader.load(configFile.toFile());
      assertEquals(0.55, committed.getDouble("Arm/PIDController/p"), 1e-9);

      Files.writeString(watchFile, "{\"version\":1,\"changed\":true}");
      watchFile.toFile().setLastModified(System.currentTimeMillis() + 1000);
      ((DoubleNode) manager.getDocument().getNode("Arm/PIDController/p")).setValue(0.66);
      TypedNetworkTableSync.publish(manager.getDocument());

      Thread.sleep(550);
      manager.periodic();

      ConfigDocument afterWatch = TypedConfigLoader.load(configFile.toFile());
      assertEquals(0.66, afterWatch.getDouble("Arm/PIDController/p"), 1e-9);
    } finally {
      manager.close();
    }
  }

  @Test
  void matchModeIgnoresPromoteAndUsesDefaultsDocument(@TempDir Path tempDirectory)
      throws Exception {
    Path configFile = tempDirectory.resolve("robot-config.json");
    Path cacheFile = tempDirectory.resolve("config-cache.json");
    Path watchFile = tempDirectory.resolve("elastic-layout.json");
    Files.copy(EXAMPLE_CONFIG, configFile);
    Files.writeString(watchFile, "{}");

    debugModeEntry().setBoolean(false);

    ConfigManager manager =
        new ConfigManager(configFile.toFile(), cacheFile.toFile(), watchFile.toFile());
    try {
      manager.periodic();
      assertFalse(
          manager.isDebugMode(),
          "DebugMode=false should force match mode when FMS is not attached");

      double defaultsP = manager.getDocument().getDouble("Arm/PIDController/p");
      assertEquals(0.1, defaultsP, 1e-9);

      manager.promote(); // ignored in match mode
      assertEquals(
          defaultsP,
          TypedConfigLoader.load(configFile.toFile()).getDouble("Arm/PIDController/p"),
          1e-9);
    } finally {
      manager.close();
    }
  }

  private static NetworkTableEntry debugModeEntry() {
    return NetworkTableInstance.getDefault().getTable("Config").getEntry("DebugMode");
  }
}
