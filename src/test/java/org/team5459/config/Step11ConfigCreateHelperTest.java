package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.team5459.config.types.DoubleNode;
import org.team5459.config.types.FolderNode;
import org.team5459.config.types.PIDControllerNode;

/** Unit tests for Create-panel default node creation. */
class Step11ConfigCreateHelperTest {
  private static final Path DEPLOY_CONFIG = Path.of("src/main/deploy/robot-config.json");

  @Test
  void createBuildsDoubleAndPidDefaults() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    assertTrue(ConfigCreateHelper.create(document, "Double", "Intake/newDouble"));
    assertTrue(document.getNode("Intake/newDouble") instanceof DoubleNode);
    assertEquals(0.0, document.getDouble("Intake/newDouble"), 1e-9);
    assertTrue(ConfigCreateHelper.create(document, "PIDController", "Intake/newPid"));
    assertTrue(document.getNode("Intake/newPid") instanceof PIDControllerNode);
    assertEquals(0.0, document.getPIDController("Intake/newPid").getP(), 1e-9);
    assertEquals(0.0, document.getPIDController("Intake/newPid").getSetpoint(), 1e-9);
    assertTrue(document.getNode("Intake/newPid/setpoint") instanceof DoubleNode);
  }

  @Test
  void createRejectsExistingAndReservedPaths() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    assertFalse(ConfigCreateHelper.create(document, "Double", "Arm/operatorOffset"));
    assertFalse(ConfigCreateHelper.create(document, "Double", "Save"));
    assertFalse(ConfigCreateHelper.create(document, "Double", ""));
  }

  @Test
  void buildPathFromFolderAndName() {
    assertEquals("gain", ConfigCreateHelper.buildPath(ConfigCreateHelper.ROOT_FOLDER, "gain"));
    assertEquals("Arm/gain", ConfigCreateHelper.buildPath("Arm", "gain"));
    assertEquals("Claw/Intake/gain", ConfigCreateHelper.buildPath("Claw/Intake", "gain"));
    assertNull(ConfigCreateHelper.buildPath("Arm", "bad/name"));
    assertNull(ConfigCreateHelper.buildPath("Arm", ""));
  }

  @Test
  void listCreatableFoldersIncludesNested() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    assertTrue(ConfigCreateHelper.create(document, ConfigCreateHelper.FOLDER_TYPE, "NestRoot"));
    assertTrue(
        ConfigCreateHelper.create(document, ConfigCreateHelper.FOLDER_TYPE, "NestRoot/Inner"));
    List<String> folders = ConfigCreateHelper.listCreatableFolders(document);
    assertTrue(folders.contains("Arm"));
    assertTrue(folders.contains("Elevator"));
    assertTrue(folders.contains("NestRoot"));
    assertTrue(folders.contains("NestRoot/Inner"));
  }

  @Test
  void createEmptyFolder() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    assertTrue(ConfigCreateHelper.create(document, ConfigCreateHelper.FOLDER_TYPE, "Intake"));
    assertTrue(document.getNode("Intake") instanceof FolderNode);
    assertTrue(ConfigCreateHelper.create(document, ConfigCreateHelper.FOLDER_TYPE, "Arm/Inner"));
    assertTrue(document.getNode("Arm/Inner") instanceof FolderNode);
    assertFalse(ConfigCreateHelper.create(document, ConfigCreateHelper.FOLDER_TYPE, "Arm"));
  }

  @Test
  void typeChooserIncludesFolder() {
    String[] options = ConfigCreateHelper.typeChooserOptions();
    assertEquals(ConfigCreateHelper.FOLDER_TYPE, options[0]);
  }
}
