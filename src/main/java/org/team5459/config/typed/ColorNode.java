package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.wpilibj.util.Color;
import java.util.Map;

/** A {@link Color} backed by typed RGB fields in the 0-255 range. */
public final class ColorNode extends ValueConfigNode<Color> {

  @JsonCreator
  public ColorNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "Color";
  }

  @Override
  protected Color buildValue() {
    ConfigFieldReader fieldReader = reader();
    return new Color(
        fieldReader.readInt("red", 0),
        fieldReader.readInt("green", 0),
        fieldReader.readInt("blue", 0));
  }

  public Color getColor() {
    return getValue();
  }
}
