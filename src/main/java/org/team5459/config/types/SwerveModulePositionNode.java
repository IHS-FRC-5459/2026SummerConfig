package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** A {@link SwerveModulePosition} backed by typed distance and angle fields. */
public final class SwerveModulePositionNode extends ValueConfigNode<SwerveModulePosition> {

  @JsonCreator
  public SwerveModulePositionNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "SwerveModulePosition";
  }

  @Override
  protected SwerveModulePosition buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new SwerveModulePosition(
        fieldReader.readDouble("distanceMeters", 0.0),
        fieldReader.readRotation2d("angle", Rotation2d.kZero));
  }

  public SwerveModulePosition getModulePosition() {
    return getValue();
  }
}
