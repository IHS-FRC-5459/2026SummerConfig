package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.trajectory.Trajectory;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ConfigWarnings;
import org.team5459.config.ValueConfigNode;

/** A {@link Trajectory.State} backed by typed trajectory state fields. */
public final class TrajectoryStateNode extends ValueConfigNode<Trajectory.State> {

  @JsonCreator
  public TrajectoryStateNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Trajectory.State";
  }

  @Override
  protected Trajectory.State buildValue() {
    ConfigFieldReader fieldReader = reader();
    ConfigNode poseNode = getFields().get("poseMeters");
    Pose2d pose = Pose2d.kZero;
    if (poseNode instanceof Pose2dNode pose2dNode) {
      pose = pose2dNode.getPose();
    } else if (poseNode != null) {
      ConfigWarnings.warnWrongFieldType("Trajectory.State", "poseMeters", "Pose2d", poseNode);
    } else {
      ConfigWarnings.warnMissingField("Trajectory.State", "poseMeters", Pose2d.kZero);
    }
    return new Trajectory.State(
        fieldReader.readDouble("timeSeconds", 0.0),
        fieldReader.readDouble("velocityMetersPerSecond", 0.0),
        fieldReader.readDouble("accelerationMetersPerSecondSq", 0.0),
        pose,
        fieldReader.readDouble("curvatureRadPerMeter", 0.0));
  }

  public Trajectory.State getState() {
    return getValue();
  }
}
