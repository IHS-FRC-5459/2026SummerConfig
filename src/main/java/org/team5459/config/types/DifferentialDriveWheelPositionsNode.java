package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelPositions;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

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
