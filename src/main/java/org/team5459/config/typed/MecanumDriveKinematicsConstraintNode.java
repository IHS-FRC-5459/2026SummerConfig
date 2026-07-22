package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.MecanumDriveKinematics;
import edu.wpi.first.math.trajectory.constraint.MecanumDriveKinematicsConstraint;
import java.util.Map;

/** A {@link MecanumDriveKinematicsConstraint} backed by typed fields. */
public final class MecanumDriveKinematicsConstraintNode
    extends ValueConfigNode<MecanumDriveKinematicsConstraint> {

  @JsonCreator
  public MecanumDriveKinematicsConstraintNode(
      @JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "MecanumDriveKinematicsConstraint";
  }

  @Override
  protected MecanumDriveKinematicsConstraint buildValue() {
    ConfigFieldReader fieldReader = reader();
    MecanumDriveKinematicsNode kinematicsNode = fieldReader.readMecanumKinematics("kinematics");
    MecanumDriveKinematics kinematics =
        kinematicsNode == null
            ? new MecanumDriveKinematics(
                Translation2d.kZero, Translation2d.kZero, Translation2d.kZero, Translation2d.kZero)
            : kinematicsNode.getKinematics();
    return new MecanumDriveKinematicsConstraint(
        kinematics, fieldReader.readDouble("maxSpeedMetersPerSecond", 0.0));
  }

  public MecanumDriveKinematicsConstraint getConstraint() {
    return getValue();
  }
}
