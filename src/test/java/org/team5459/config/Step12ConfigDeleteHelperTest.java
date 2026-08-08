package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.team5459.config.types.DoubleNode;
import org.team5459.config.types.FolderNode;

/** Unit tests for debug Delete-panel path listing and removal. */
class Step12ConfigDeleteHelperTest {
  private static final Path DEPLOY_CONFIG = Path.of("src/main/deploy/robot-config.json");

  @Test
  void listIncludesNestedLeavesAndFolders() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    List<String> paths = ConfigDeleteHelper.listDeletablePaths(document);
    assertTrue(paths.contains("Arm"));
    assertTrue(paths.contains("Arm/operatorOffset"));
    assertTrue(paths.contains("Arm/PIDController"));
    assertFalse(paths.contains("Arm/PIDController/p"));
    assertFalse(paths.contains("Arm/PIDController/setpoint"));
    assertFalse(paths.contains(ConfigDeleteHelper.NONE_OPTION));
  }

  @Test
  void deleteLeafRemovesOnlyThatPath() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    assertTrue(ConfigDeleteHelper.delete(document, "Arm/operatorOffset"));
    assertNull(document.getNodeQuiet("Arm/operatorOffset"));
    assertTrue(document.getNodeQuiet("Arm") instanceof FolderNode);
    assertTrue(document.getNodeQuiet("Arm/PIDController/p") instanceof DoubleNode);
  }

  @Test
  void deleteRejectsCompositeChildren() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    assertFalse(ConfigDeleteHelper.delete(document, "Arm/PIDController/p"));
    assertTrue(document.getNodeQuiet("Arm/PIDController/p") instanceof DoubleNode);
    assertTrue(ConfigDeleteHelper.delete(document, "Arm/PIDController"));
    assertNull(document.getNodeQuiet("Arm/PIDController"));
  }

  @Test
  void deleteFolderRemovesChildren() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    assertTrue(ConfigDeleteHelper.delete(document, "Arm"));
    assertNull(document.getNodeQuiet("Arm"));
    assertNull(document.getNodeQuiet("Arm/operatorOffset"));
    assertNull(document.getNodeQuiet("Arm/PIDController"));
    assertNull(document.getNodeQuiet("Arm/PIDController/p"));
  }

  @Test
  void deleteRejectsNoneAndMissing() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    assertFalse(ConfigDeleteHelper.delete(document, ConfigDeleteHelper.NONE_OPTION));
    assertFalse(ConfigDeleteHelper.delete(document, "DoesNotExist"));
    assertFalse(ConfigDeleteHelper.delete(document, "Save"));
  }

  @Test
  void removePathApiWorks() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    assertTrue(document.removePath("Elevator/myBool"));
    assertFalse(document.hasPath("Elevator/myBool"));
    assertTrue(document.hasPath("Elevator/PIDController"));
  }

  @Test
  void deletedPathsAreSuppressedFromAutoRegister() {
    ConfigDeletedPaths.allow("tmp/suppressTest");
    ConfigDeletedPaths.suppress("tmp/suppressTest");
    assertTrue(ConfigDeletedPaths.isSuppressed("tmp/suppressTest"));
    assertTrue(ConfigDeletedPaths.isSuppressed("tmp/suppressTest/child"));
    ConfigDeletedPaths.allow("tmp/suppressTest");
    assertFalse(ConfigDeletedPaths.isSuppressed("tmp/suppressTest"));
  }
}
