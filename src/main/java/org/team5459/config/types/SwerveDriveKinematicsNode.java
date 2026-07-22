package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** A {@link SwerveDriveKinematics} backed by typed module translation fields. */
public final class SwerveDriveKinematicsNode extends ValueConfigNode<SwerveDriveKinematics> {

  @JsonCreator
  public SwerveDriveKinematicsNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "SwerveDriveKinematics";
  }

  @Override
  protected SwerveDriveKinematics buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new SwerveDriveKinematics(
        fieldReader.readTranslation2d("frontLeft", Translation2d.kZero),
        fieldReader.readTranslation2d("frontRight", Translation2d.kZero),
        fieldReader.readTranslation2d("rearLeft", Translation2d.kZero),
        fieldReader.readTranslation2d("rearRight", Translation2d.kZero));
  }

  public SwerveDriveKinematics getKinematics() {
    return getValue();
  }
}
