package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.MecanumDriveKinematics;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** A {@link MecanumDriveKinematics} backed by typed module translation fields. */
public final class MecanumDriveKinematicsNode extends ValueConfigNode<MecanumDriveKinematics> {

  @JsonCreator
  public MecanumDriveKinematicsNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "MecanumDriveKinematics";
  }

  @Override
  protected MecanumDriveKinematics buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new MecanumDriveKinematics(
        fieldReader.readTranslation2d("frontLeft", Translation2d.kZero),
        fieldReader.readTranslation2d("frontRight", Translation2d.kZero),
        fieldReader.readTranslation2d("rearLeft", Translation2d.kZero),
        fieldReader.readTranslation2d("rearRight", Translation2d.kZero));
  }

  public MecanumDriveKinematics getKinematics() {
    return getValue();
  }
}
