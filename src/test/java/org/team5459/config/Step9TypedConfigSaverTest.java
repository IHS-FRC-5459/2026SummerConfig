package org.team5459.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.team5459.config.typed.ConfigDocument;
import org.team5459.config.typed.TypedConfigLoader;
import org.team5459.config.typed.TypedConfigSaver;

/** Verifies typed config saves only schema fields, not runtime Java objects. */
class Step9TypedConfigSaverTest {
  private static final Path EXAMPLE_CONFIG =
      Path.of("src/test/resources/typed-example-config.json");

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
}
