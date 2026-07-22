package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import java.util.Map;

/** A {@link SwerveModuleState} backed by typed speed and angle fields. */
public final class SwerveModuleStateNode extends ValueConfigNode<SwerveModuleState> {

  @JsonCreator
  public SwerveModuleStateNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "SwerveModuleState";
  }

  @Override
  protected SwerveModuleState buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new SwerveModuleState(
        fieldReader.readDouble("speedMetersPerSecond", 0.0),
        fieldReader.readRotation2d("angle", Rotation2d.kZero));
  }

  public SwerveModuleState getModuleState() {
    return getValue();
  }
}
