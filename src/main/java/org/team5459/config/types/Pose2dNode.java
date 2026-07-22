package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/**
 * {@link Pose2d} built from typed translation and rotation child fields.
 *
 * <p>Translation may be supplied either as a nested {@code Translation2d} or inline {@code x}/{@code
 * y} doubles. Rotation accepts, in priority order: nested {@code rotation}, {@code deg}, {@code
 * heading}, or defaults to zero.
 */
public final class Pose2dNode extends ValueConfigNode<Pose2d> {

  @JsonCreator
  public Pose2dNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Pose2d";
  }

  @Override
  protected Pose2d buildValue() {
    ConfigFieldReader fieldReader = reader();
    Translation2d translation;
    if (getFields().containsKey("translation")) {
      translation = fieldReader.readTranslation2d("translation", Translation2d.kZero);
    } else {
      translation =
          new Translation2d(fieldReader.readDouble("x", 0.0), fieldReader.readDouble("y", 0.0));
    }
    Rotation2d rotation;
    if (getFields().containsKey("rotation")) {
      rotation = fieldReader.readRotation2d("rotation", Rotation2d.kZero);
    } else if (getFields().containsKey("deg")) {
      rotation = Rotation2d.fromDegrees(fieldReader.readDouble("deg", 0.0));
    } else if (getFields().containsKey("heading")) {
      rotation = fieldReader.readRotation2d("heading", Rotation2d.kZero);
    } else {
      rotation = Rotation2d.kZero;
    }
    return new Pose2d(translation, rotation);
  }

  public Pose2d getPose() {
    return getValue();
  }
}
