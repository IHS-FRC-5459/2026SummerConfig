package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.trajectory.constraint.SwerveDriveKinematicsConstraint;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** A {@link SwerveDriveKinematicsConstraint} backed by typed fields. */
public final class SwerveDriveKinematicsConstraintNode
    extends ValueConfigNode<SwerveDriveKinematicsConstraint> {

  @JsonCreator
  public SwerveDriveKinematicsConstraintNode(
      @JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "SwerveDriveKinematicsConstraint";
  }

  @Override
  protected SwerveDriveKinematicsConstraint buildValue() {
    ConfigFieldReader fieldReader = reader();
    SwerveDriveKinematicsNode kinematicsNode = fieldReader.readSwerveKinematics("kinematics");
    SwerveDriveKinematics kinematics =
        kinematicsNode == null
            ? new SwerveDriveKinematics(
                Translation2d.kZero, Translation2d.kZero, Translation2d.kZero, Translation2d.kZero)
            : kinematicsNode.getKinematics();
    return new SwerveDriveKinematicsConstraint(
        kinematics, fieldReader.readDouble("maxSpeedMetersPerSecond", 0.0));
  }

  public SwerveDriveKinematicsConstraint getConstraint() {
    return getValue();
  }
}
