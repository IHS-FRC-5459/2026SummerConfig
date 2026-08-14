package org.team5459.config;

import java.util.Map;
import org.team5459.config.types.FolderNode;
import org.team5459.config.types.PIDControllerNode;
import org.team5459.config.types.ProfiledPIDControllerNode;

/**
 * Maps config node classes to Elastic / Shuffleboard SmartDashboard type strings.
 *
 * <p>Elastic multi-topic widgets (see <a
 * href="https://frc-elastic.gitbook.io/docs/additional-features-and-references/widgets-list-and-properties-reference">Widgets
 * List &amp; Properties Reference</a>) discover Sendable-style tables via a {@code .type} string
 * topic under the table. Publishing that marker lets Elastic offer the matching widget (e.g. {@code
 * PIDController}) instead of only raw scalar leaves.
 */
public final class ConfigElasticTypes {

  private static final Map<Class<? extends ConfigNode>, String> ELASTIC_TYPES =
      Map.of(
          FolderNode.class, "Folder",
          PIDControllerNode.class, "PIDController",
          ProfiledPIDControllerNode.class, "ProfiledPIDController");

  private ConfigElasticTypes() {}

  /**
   * Returns the Elastic/Sendable type string for {@code node}, or {@code null} if there is no
   * matching multi-topic widget.
   */
  public static String elasticTypeFor(ConfigNode node) {
    if (node == null) {
      return null;
    }
    return ELASTIC_TYPES.get(node.getClass());
  }

  /** NT entry name used by WPILib Sendables / Elastic for widget type discovery. */
  public static final String TYPE_TOPIC = ".type";
}
