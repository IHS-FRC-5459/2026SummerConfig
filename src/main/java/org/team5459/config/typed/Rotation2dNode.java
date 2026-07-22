package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Rotation2d;
import java.util.Map;

/** A {@link Rotation2d} backed by typed {@code deg} or {@code rad} fields. */
public final class Rotation2dNode extends ValueConfigNode<Rotation2d> {

  @JsonCreator
  public Rotation2dNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Rotation2d";
  }

  @Override
  protected Rotation2d buildValue() {
    ConfigFieldReader fieldReader = reader();
    if (getFields().containsKey("deg")) {
      return Rotation2d.fromDegrees(fieldReader.readDouble("deg", 0.0));
    }
    return new Rotation2d(fieldReader.readDouble("rad", 0.0));
  }

  @JsonIgnore
  public Rotation2d getRotation() {
    return getValue();
  }

  /** Rebuilds the rotation from the current field values. */
  public void syncRotation() {
    syncValue();
  }
}
