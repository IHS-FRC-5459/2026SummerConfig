package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rectangle2d;
import edu.wpi.first.math.geometry.Rotation2d;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** A {@link Rectangle2d} backed by typed center and dimension fields. */
public final class Rectangle2dNode extends ValueConfigNode<Rectangle2d> {

  @JsonCreator
  public Rectangle2dNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Rectangle2d";
  }

  @Override
  protected Rectangle2d buildValue() {
    ConfigFieldReader fieldReader = reader();
    Pose2d center;
    ConfigNode centerNode = getFields().get("center");
    if (centerNode instanceof Pose2dNode poseNode) {
      center = poseNode.getPose();
    } else {
      center =
          new Pose2d(
              fieldReader.readDouble("centerX", 0.0),
              fieldReader.readDouble("centerY", 0.0),
              fieldReader.readRotation2d("rotation", Rotation2d.kZero));
    }
    return new Rectangle2d(
        center, fieldReader.readDouble("xWidth", 0.0), fieldReader.readDouble("yWidth", 0.0));
  }

  public Rectangle2d getRectangle() {
    return getValue();
  }
}
