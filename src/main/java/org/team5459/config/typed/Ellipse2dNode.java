package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Ellipse2d;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import java.util.Map;

/** An {@link Ellipse2d} backed by typed center and axis fields. */
public final class Ellipse2dNode extends ValueConfigNode<Ellipse2d> {

  @JsonCreator
  public Ellipse2dNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Ellipse2d";
  }

  @Override
  protected Ellipse2d buildValue() {
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
    return new Ellipse2d(
        center, fieldReader.readDouble("xSemiAxis", 0.0), fieldReader.readDouble("ySemiAxis", 0.0));
  }

  public Ellipse2d getEllipse() {
    return getValue();
  }
}
