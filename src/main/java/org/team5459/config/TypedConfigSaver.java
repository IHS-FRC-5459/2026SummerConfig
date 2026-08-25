package org.team5459.config;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Persists an in-memory {@link ConfigDocument} back to disk.
 *
 * <p>Saving uses {@link ConfigJsonWriter} rather than Jackson serialization so the file contains
 * only the typed schema. This keeps round-trips stable even when node classes expose extra runtime
 * getters.
 */
public final class TypedConfigSaver {

  private TypedConfigSaver() {}

  /**
   * Writes a typed configuration document to disk.
   *
   * <p>Writes directly to {@code jsonFile}. Suitable for frequent, low-stakes writes (e.g. the live
   * tuning cache file), where losing an in-progress write to an interruption is an acceptable risk.
   * For the committed config file, prefer {@link #saveSafely(File, ConfigDocument)}.
   *
   * @param jsonFile Destination file
   * @param document Configuration document to save
   */
  public static void save(File jsonFile, ConfigDocument document) {
    try {
      ConfigJsonWriter.write(jsonFile, document.getRootEntries());
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Unable to save typed config file: " + jsonFile.getAbsolutePath(), exception);
    }
  }

  /**
   * Writes a typed configuration document to disk, guarding against a corrupted or interrupted
   * write.
   *
   * <p>The document is first written to a temporary file and validated as well-formed JSON. Only if
   * that succeeds is {@code jsonFile} touched at all: the previous contents (if any) are copied to
   * a {@code .bak} sibling file, then the validated temp file atomically replaces {@code jsonFile}.
   * If validation fails, {@code jsonFile} is left completely untouched and an exception is thrown.
   *
   * <p>Intended for the committed config file (e.g. {@code robot-config.json} on promote/Save),
   * where a half-written or corrupted file would be read back at the next boot.
   *
   * @param jsonFile Destination file
   * @param document Configuration document to save
   * @throws UncheckedIOException if writing, validating, or committing the file fails; {@code
   *     jsonFile} is guaranteed unchanged in this case
   */
  public static void saveSafely(File jsonFile, ConfigDocument document) {
    File parent = jsonFile.getAbsoluteFile().getParentFile();
    File tempFile = new File(parent, jsonFile.getName() + ".tmp");
    File backupFile = new File(parent, jsonFile.getName() + ".bak");

    try {
      // 1. Write to a temp file; the real file is not touched yet.
      ConfigJsonWriter.write(tempFile, document.getRootEntries());

      // 2. Validate the temp file is well-formed JSON before trusting it.
      try {
        TypedConfigMapper.mapper().readTree(tempFile);
      } catch (IOException validationFailure) {
        Files.deleteIfExists(tempFile.toPath());
        throw new UncheckedIOException(
            "Refusing to commit config: written file failed validation ("
                + tempFile.getAbsolutePath()
                + "). "
                + jsonFile.getName()
                + " was not modified.",
            validationFailure);
      }

      // 3. Back up the current file (if any) before replacing it.
      if (jsonFile.exists()) {
        Files.copy(jsonFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }

      // 4. Atomically swap the validated temp file into place.
      try {
        Files.move(
            tempFile.toPath(),
            jsonFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException nonAtomicFilesystem) {
        Files.move(tempFile.toPath(), jsonFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Unable to safely save typed config file: " + jsonFile.getAbsolutePath(), exception);
    }
  }
}
