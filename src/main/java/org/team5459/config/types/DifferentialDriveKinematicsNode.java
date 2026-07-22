package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import java.util.Map;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** A {@link DifferentialDriveKinematics} backed by typed geometry fields. */
public final class DifferentialDriveKinematicsNode
    extends ValueConfigNode<DifferentialDriveKinematics> {

  @JsonCreator
  public DifferentialDriveKinematicsNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "DifferentialDriveKinematics";
  }

  @Override
  protected DifferentialDriveKinematics buildValue() {
    return new DifferentialDriveKinematics(reader().readDouble("trackWidthMeters", 0.0));
  }

  public DifferentialDriveKinematics getKinematics() {
    return getValue();
  }
}
