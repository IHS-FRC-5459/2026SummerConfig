package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** A {@link MecanumDrive.WheelSpeeds} backed by typed normalized wheel speed fields. */
public final class MecanumDriveWheelSpeedsNode extends ValueConfigNode<MecanumDrive.WheelSpeeds> {

  @JsonCreator
  public MecanumDriveWheelSpeedsNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "MecanumDrive.WheelSpeeds";
  }

  @Override
  protected MecanumDrive.WheelSpeeds buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new MecanumDrive.WheelSpeeds(
        fieldReader.readDouble("frontLeft", 0.0),
        fieldReader.readDouble("frontRight", 0.0),
        fieldReader.readDouble("rearLeft", 0.0),
        fieldReader.readDouble("rearRight", 0.0));
  }

  public MecanumDrive.WheelSpeeds getWheelSpeeds() {
    return getValue();
  }
}
