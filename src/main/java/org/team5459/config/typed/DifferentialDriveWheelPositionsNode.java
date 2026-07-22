package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelPositions;
import java.util.Map;

/** A {@link DifferentialDriveWheelPositions} backed by typed wheel distance fields. */
public final class DifferentialDriveWheelPositionsNode
    extends ValueConfigNode<DifferentialDriveWheelPositions> {

  @JsonCreator
  public DifferentialDriveWheelPositionsNode(
      @JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "DifferentialDriveWheelPositions";
  }

  @Override
  protected DifferentialDriveWheelPositions buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new DifferentialDriveWheelPositions(
        fieldReader.readDouble("leftMeters", 0.0), fieldReader.readDouble("rightMeters", 0.0));
  }

  public DifferentialDriveWheelPositions getWheelPositions() {
    return getValue();
  }
}
