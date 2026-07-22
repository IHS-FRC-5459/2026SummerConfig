package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Rotation3d;
import java.util.Map;

/** A {@link Rotation3d} backed by a nested {@link Quaternion} or roll/pitch/yaw fields. */
public final class Rotation3dNode extends ValueConfigNode<Rotation3d> {

  @JsonCreator
  public Rotation3dNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Rotation3d";
  }

  @Override
  protected Rotation3d buildValue() {
    ConfigFieldReader fieldReader = reader();
    if (getFields().containsKey("quaternion")) {
      ConfigNode node = getFields().get("quaternion");
      if (node instanceof QuaternionNode quaternionNode) {
        return new Rotation3d(quaternionNode.getQuaternion());
      }
      ConfigWarnings.warnWrongFieldType("Rotation3d", "quaternion", "Quaternion", node);
      return new Rotation3d();
    }
    return new Rotation3d(
        fieldReader.readDouble("roll", 0.0),
        fieldReader.readDouble("pitch", 0.0),
        fieldReader.readDouble("yaw", 0.0));
  }

  public Rotation3d getRotation() {
    return getValue();
  }
}
