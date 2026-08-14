package org.team5459.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Lightweight poller that detects content changes on a watched JSON file (e.g. Elastic Save As
 * rewriting {@code elastic-layout.json}) and runs a promote callback.
 *
 * <p>Cost is a {@code lastModified}/{@code length} check each poll; content is hashed only when
 * those metadata values change, so idle loops stay cheap.
 */
public final class ConfigPromoteWatcher {
  private static final long kDefaultMinIntervalMs = 500L;

  private final File watchedFile;
  private final Runnable onContentChanged;
  private final long minIntervalMs;

  private long lastPollMs;
  private long lastModified = Long.MIN_VALUE;
  private long lastLength = Long.MIN_VALUE;
  private byte[] lastDigest;
  private boolean initialized;

  public ConfigPromoteWatcher(File watchedFile, Runnable onContentChanged) {
    this(watchedFile, onContentChanged, kDefaultMinIntervalMs);
  }

  public ConfigPromoteWatcher(File watchedFile, Runnable onContentChanged, long minIntervalMs) {
    this.watchedFile = watchedFile;
    this.onContentChanged = onContentChanged;
    this.minIntervalMs = Math.max(100L, minIntervalMs);
  }

  /**
   * Polls for a content change. Safe to call from {@code robotPeriodic}; returns quickly when
   * nothing changed.
   */
  public void poll() {
    long now = System.currentTimeMillis();
    if (now - lastPollMs < minIntervalMs) {
      return;
    }
    lastPollMs = now;

    if (watchedFile == null || !watchedFile.isFile()) {
      return;
    }

    long modified = watchedFile.lastModified();
    long length = watchedFile.length();
    if (initialized && modified == lastModified && length == lastLength) {
      return;
    }

    byte[] digest = digestFile(watchedFile);
    if (digest == null) {
      return;
    }

    if (!initialized) {
      initialized = true;
      lastModified = modified;
      lastLength = length;
      lastDigest = digest;
      return;
    }

    lastModified = modified;
    lastLength = length;
    if (Arrays.equals(digest, lastDigest)) {
      return;
    }
    lastDigest = digest;
    onContentChanged.run();
  }

  File getWatchedFile() {
    return watchedFile;
  }

  private static byte[] digestFile(File file) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return digest.digest(Files.readAllBytes(file.toPath()));
    } catch (IOException | NoSuchAlgorithmException exception) {
      ConfigWarnings.warn(
          "Failed to hash watched config file " + file + ": " + exception.getMessage());
      return null;
    }
  }
}
