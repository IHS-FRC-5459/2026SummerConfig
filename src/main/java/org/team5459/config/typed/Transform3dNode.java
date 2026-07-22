package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import java.util.Map;

/** A {@link Transform3d} backed by typed translation and rotation fields. */
public final class Transform3dNode extends ValueConfigNode<Transform3d> {

  @JsonCreator
  public Transform3dNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Transform3d";
  }

  @Override
  protected Transform3d buildValue() {
    ConfigFieldReader fieldReader = reader();
    Translation3d translation;
    if (getFields().containsKey("translation")) {
      translation = fieldReader.readTranslation3d("translation", Translation3d.kZero);
    } else {
      translation =
          new Translation3d(
              fieldReader.readDouble("x", 0.0),
              fieldReader.readDouble("y", 0.0),
              fieldReader.readDouble("z", 0.0));
    }
    Rotation3d rotation = fieldReader.readRotation3d("rotation", Rotation3d.kZero);
    return new Transform3d(translation, rotation);
  }

  public Transform3d getTransform() {
    return getValue();
  }
}
