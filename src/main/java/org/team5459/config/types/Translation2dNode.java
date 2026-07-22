package org.team5459.config.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.math.geometry.Translation2d;
import java.util.Map;
import org.team5459.config.ConfigFieldReader;
import org.team5459.config.ConfigNode;
import org.team5459.config.ValueConfigNode;

/** A {@link Translation2d} backed by typed {@code x} and {@code y} fields in meters. */
public final class Translation2dNode extends ValueConfigNode<Translation2d> {

  @JsonCreator
  public Translation2dNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Translation2d";
  }

  @Override
  protected Translation2d buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new Translation2d(fieldReader.readDouble("x", 0.0), fieldReader.readDouble("y", 0.0));
  }

  public Translation2d getTranslation() {
    return getValue();
  }
}
