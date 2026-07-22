package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/**
 * {@link Pose3d} with the same flexible field layouts as {@link Pose2dNode}, extended to 3D.
 *
 * <p>Translation accepts nested {@code translation} or inline {@code x}/{@code y}/{@code z}.
 * Rotation accepts a nested {@code Rotation3d} child.
 */
public final class Pose3dNode extends ValueConfigNode<Pose3d> {

  @JsonCreator
  public Pose3dNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Pose3d";
  }

  @Override
  protected Pose3d buildValue() {
    ConfigFieldReader fieldReader = reader();
    Translation3d translation;
    if (getFields().containsKey("translation")) {
      translation = fieldReader.readTranslation3d("translation", Translation3d.kZero);
    } else {
      translation =
          new Translation3d(
              fieldReader.readDouble("x", 0.0),
              fieldReader.readDouble("y", 0.0),
              fieldReader.readDouble("z", 0.0));
    }
    Rotation3d rotation = fieldReader.readRotation3d("rotation", Rotation3d.kZero);
    return new Pose3d(translation, rotation);
  }

  public Pose3d getPose() {
    return getValue();
  }
}
