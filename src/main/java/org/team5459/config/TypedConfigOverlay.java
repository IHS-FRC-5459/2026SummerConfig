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
 * Copies scalar/array leaf values from one typed document onto another where paths and types match,
 * and inserts missing paths from the source (used when restoring {@code config-cache.json}).
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

  /** Overlays matching values from {@code source} onto {@code target}, inserting missing paths. */
  public static void apply(ConfigDocument source, ConfigDocument target) {
    mergeEntries(source.getRootEntries(), target, "");
  }

  private static void mergeEntries(
      Map<String, ConfigNode> source, ConfigDocument target, String pathPrefix) {
    source.forEach(
        (name, sourceNode) -> {
          String path = pathPrefix.isEmpty() ? name : pathPrefix + "/" + name;
          if (sourceNode instanceof FolderNode sourceFolder) {
            if (!target.hasPath(path)) {
              target.insertLeaf(path, new FolderNode(new java.util.LinkedHashMap<>()));
            }
            mergeEntries(sourceFolder.getChildren(), target, path);
            return;
          }

          if (!target.hasPath(path)) {
            target.insertLeaf(path, sourceNode);
            return;
          }

          ConfigNode targetNode = target.getNode(path);
          if (targetNode == null) {
            return;
          }
          overlayExisting(sourceNode, targetNode);
        });
  }

  private static void overlayExisting(ConfigNode source, ConfigNode target) {
    if (source instanceof CompositeConfigNode sourceComposite
        && target instanceof CompositeConfigNode targetComposite) {
      sourceComposite
          .getFields()
          .forEach(
              (fieldName, sourceField) -> {
                ConfigNode targetField = targetComposite.getFields().get(fieldName);
                if (targetField == null) {
                  return;
                }
                overlayExisting(sourceField, targetField);
              });
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
