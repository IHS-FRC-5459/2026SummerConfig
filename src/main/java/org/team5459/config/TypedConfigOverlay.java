package org.team5459.config;

import java.io.File;
import java.util.Map;
import org.team5459.config.types.BooleanNode;
import org.team5459.config.types.DoubleArrayNode;
import org.team5459.config.types.DoubleNode;
import org.team5459.config.types.FolderNode;
import org.team5459.config.types.IntArrayNode;
import org.team5459.config.types.IntNode;
import org.team5459.config.types.StringNode;

/**
 * Copies scalar/array leaf values from one typed document onto another where paths and types match.
 *
 * <p>Used to overlay {@code config-cache.json} onto {@code robot-config.json} defaults at debug
 * startup, and to restore defaults when leaving debug mode.
 */
public final class TypedConfigOverlay {

  private TypedConfigOverlay() {}

  /**
   * Overlays {@code cacheFile} onto {@code target} when the file exists. Missing file is a no-op.
   */
  public static void applyFile(File cacheFile, ConfigDocument target) {
    if (cacheFile == null || !cacheFile.isFile()) {
      return;
    }
    ConfigDocument cache = TypedConfigLoader.load(cacheFile);
    apply(cache, target);
    System.out.println("Applied config cache from " + cacheFile.getAbsolutePath());
  }

  /** Overlays matching leaf values from {@code source} onto {@code target}. */
  public static void apply(ConfigDocument source, ConfigDocument target) {
    applyEntries(source.getRootEntries(), target.getRootEntries());
  }

  private static void applyEntries(Map<String, ConfigNode> source, Map<String, ConfigNode> target) {
    source.forEach(
        (name, sourceNode) -> {
          ConfigNode targetNode = target.get(name);
          if (targetNode == null) {
            return;
          }
          applyNode(sourceNode, targetNode);
        });
  }

  private static void applyNode(ConfigNode source, ConfigNode target) {
    if (source instanceof FolderNode sourceFolder && target instanceof FolderNode targetFolder) {
      applyEntries(sourceFolder.getChildren(), targetFolder.getChildren());
      return;
    }
    if (source instanceof CompositeConfigNode sourceComposite
        && target instanceof CompositeConfigNode targetComposite) {
      applyEntries(sourceComposite.getFields(), targetComposite.getFields());
      targetComposite.applyFieldChanges();
      return;
    }
    if (source instanceof DoubleNode sourceDouble && target instanceof DoubleNode targetDouble) {
      targetDouble.setValue(sourceDouble.getValue());
    } else if (source instanceof IntNode sourceInt && target instanceof IntNode targetInt) {
      targetInt.setValue(sourceInt.getValue());
    } else if (source instanceof BooleanNode sourceBoolean
        && target instanceof BooleanNode targetBoolean) {
      targetBoolean.setValue(sourceBoolean.getValue());
    } else if (source instanceof StringNode sourceString
        && target instanceof StringNode targetString) {
      targetString.setValue(sourceString.getValue());
    } else if (source instanceof DoubleArrayNode sourceArray
        && target instanceof DoubleArrayNode targetArray) {
      targetArray.setValue(sourceArray.getValue());
    } else if (source instanceof IntArrayNode sourceArray
        && target instanceof IntArrayNode targetArray) {
      targetArray.setValue(sourceArray.getValue());
    }
  }
}
