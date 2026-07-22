package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/**
 * {@link Transform2d} with flexible translation and rotation field layouts.
 *
 * <p>Translation accepts nested {@code translation} or inline {@code x}/{@code y}. Rotation
 * accepts nested {@code rotation} and/or a top-level {@code deg} override.
 */
public final class Transform2dNode extends ValueConfigNode<Transform2d> {

  @JsonCreator
  public Transform2dNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Transform2d";
  }

  @Override
  protected Transform2d buildValue() {
    ConfigFieldReader fieldReader = reader();
    Translation2d translation;
    if (getFields().containsKey("translation")) {
      translation = fieldReader.readTranslation2d("translation", Translation2d.kZero);
    } else {
      translation =
          new Translation2d(fieldReader.readDouble("x", 0.0), fieldReader.readDouble("y", 0.0));
    }
    Rotation2d rotation = fieldReader.readRotation2d("rotation", Rotation2d.kZero);
    if (getFields().containsKey("deg")) {
      rotation = Rotation2d.fromDegrees(fieldReader.readDouble("deg", 0.0));
    }
    return new Transform2d(translation, rotation);
  }

  public Transform2d getTransform() {
    return getValue();
  }
}
