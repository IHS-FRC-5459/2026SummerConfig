package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Translation3d;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** A {@link Translation3d} backed by typed {@code x}, {@code y}, and {@code z} fields. */
public final class Translation3dNode extends ValueConfigNode<Translation3d> {

  @JsonCreator
  public Translation3dNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Translation3d";
  }

  @Override
  protected Translation3d buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new Translation3d(
        fieldReader.readDouble("x", 0.0),
        fieldReader.readDouble("y", 0.0),
        fieldReader.readDouble("z", 0.0));
  }

  public Translation3d getTranslation() {
    return getValue();
  }
}
