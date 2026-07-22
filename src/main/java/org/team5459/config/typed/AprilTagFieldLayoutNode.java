package org.team5459.config.typed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import java.util.Map;

/**
 * An {@link AprilTagFieldLayout} loaded from an official {@link AprilTagFields} name.
 *
 * <p>Example field value: {@code k2026RebuiltWelded}. Optional {@code origin} accepts {@link
 * AprilTagFieldLayout.OriginPosition} enum names such as {@code kBlueOrigin}.
 */
public final class AprilTagFieldLayoutNode extends ValueConfigNode<AprilTagFieldLayout> {

  @JsonCreator
  public AprilTagFieldLayoutNode(@JsonProperty("value") Map<String, ConfigNode> fields) {
    super(fields);
  }

  @Override
  protected String typeName() {
    return "AprilTagFieldLayout";
  }

  @Override
  protected AprilTagFieldLayout buildValue() {
    ConfigFieldReader fieldReader = reader();
    String fieldName = fieldReader.readString("field", "kDefaultField");
    AprilTagFieldLayout layout = loadOfficialField(fieldName);
    if (getFields().containsKey("origin")) {
      String originName = fieldReader.readString("origin", "kBlueOrigin");
      try {
        layout.setOrigin(AprilTagFieldLayout.OriginPosition.valueOf(originName));
      } catch (IllegalArgumentException exception) {
        ConfigWarnings.warnWrongFieldType(
            typeName(), "origin", "AprilTagFieldLayout.OriginPosition", null);
      }
    }
    return layout;
  }

  public AprilTagFieldLayout getLayout() {
    return getValue();
  }

  private AprilTagFieldLayout loadOfficialField(String fieldName) {
    try {
      return AprilTagFieldLayout.loadField(AprilTagFields.valueOf(fieldName));
    } catch (IllegalArgumentException exception) {
      ConfigWarnings.warnWrongFieldType(typeName(), "field", "AprilTagFields", null);
      return AprilTagFieldLayout.loadField(AprilTagFields.kDefaultField);
    }
  }
}
