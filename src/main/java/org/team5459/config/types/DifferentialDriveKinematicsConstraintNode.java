package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.trajectory.constraint.DifferentialDriveKinematicsConstraint;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** A {@link DifferentialDriveKinematicsConstraint} backed by typed fields. */
public final class DifferentialDriveKinematicsConstraintNode
    extends ValueConfigNode<DifferentialDriveKinematicsConstraint> {

  @JsonCreator
  public DifferentialDriveKinematicsConstraintNode(
      @JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "DifferentialDriveKinematicsConstraint";
  }

  @Override
  protected DifferentialDriveKinematicsConstraint buildValue() {
    ConfigFieldReader fieldReader = reader();
    DifferentialDriveKinematics kinematics =
        fieldReader.readDifferentialKinematics("kinematics") == null
            ? new DifferentialDriveKinematics(0.0)
            : fieldReader.readDifferentialKinematics("kinematics").getKinematics();
    return new DifferentialDriveKinematicsConstraint(
        kinematics, fieldReader.readDouble("maxSpeedMetersPerSecond", 0.0));
  }

  public DifferentialDriveKinematicsConstraint getConstraint() {
    return getValue();
  }
}
