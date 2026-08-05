package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.team5459.config.types.DoubleNode;
import org.team5459.config.types.PIDControllerNode;

/** Unit tests for template clone creation without NetworkTables. */
class Step11ConfigCreateHelperTest {
  private static final Path DEPLOY_CONFIG = Path.of("src/main/deploy/robot-config.json");

  @Test
  void createClonesDoubleAndPidFromTemplates() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    assertTrue(ConfigCreateHelper.create(document, "Double", "Intake/newDouble"));
    assertTrue(document.getNode("Intake/newDouble") instanceof DoubleNode);
    assertTrue(ConfigCreateHelper.create(document, "PIDController", "Intake/newPid"));
    assertTrue(document.getNode("Intake/newPid") instanceof PIDControllerNode);
  }

  @Test
  void createRejectsExistingAndTemplatesPaths() {
    ConfigDocument document = TypedConfigLoader.load(DEPLOY_CONFIG.toFile());
    assertFalse(ConfigCreateHelper.create(document, "Double", "Arm/operatorOffset"));
    assertFalse(ConfigCreateHelper.create(document, "Double", "templates/nope"));
    assertFalse(ConfigCreateHelper.create(document, "Double", "Save"));
    assertFalse(ConfigCreateHelper.create(document, "Double", ""));
  }
}
