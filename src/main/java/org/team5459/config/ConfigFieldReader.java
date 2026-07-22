package org.team5459.config;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Distance;
import java.util.Map;
import org.team5459.config.types.*;

/** Reads typed child nodes from a composite config entry. */
public final class ConfigFieldReader {

  private final Map<String, ConfigNode> fields;
  private final String typeName;

  public ConfigFieldReader(Map<String, ConfigNode> fields, String typeName) {
    this.fields = fields;
    this.typeName = typeName;
  }

  public double readDouble(String name, double defaultValue) {
    ConfigNode node = fields.get(name);
    if (node instanceof DoubleNode doubleNode) {
      return doubleNode.getValue();
    }
    if (node != null) {
      ConfigWarnings.warnWrongFieldType(typeName, name, "double", node);
    } else {
      ConfigWarnings.warnMissingField(typeName, name, defaultValue);
    }
    return defaultValue;
  }

  public int readInt(String name, int defaultValue) {
    ConfigNode node = fields.get(name);
    if (node instanceof IntNode intNode) {
      return intNode.getValue();
    }
    if (node != null) {
      ConfigWarnings.warnWrongFieldType(typeName, name, "int", node);
    } else {
      ConfigWarnings.warnMissingField(typeName, name, defaultValue);
    }
    return defaultValue;
  }

  public boolean readBoolean(String name, boolean defaultValue) {
    ConfigNode node = fields.get(name);
    if (node instanceof BooleanNode booleanNode) {
      return booleanNode.getValue();
    }
    if (node != null) {
      ConfigWarnings.warnWrongFieldType(typeName, name, "boolean", node);
    } else {
      ConfigWarnings.warnMissingField(typeName, name, defaultValue);
    }
    return defaultValue;
  }

  public String readString(String name, String defaultValue) {
    ConfigNode node = fields.get(name);
    if (node instanceof StringNode stringNode) {
      return stringNode.getValue();
    }
    if (node != null) {
      ConfigWarnings.warnWrongFieldType(typeName, name, "String", node);
    } else {
      ConfigWarnings.warnMissingField(typeName, name, defaultValue);
    }
    return defaultValue;
  }

  public Rotation2d readRotation2d(String name, Rotation2d defaultValue) {
    ConfigNode node = fields.get(name);
    if (node instanceof Rotation2dNode rotationNode) {
      return rotationNode.getRotation();
    }
    if (node != null) {
      ConfigWarnings.warnWrongFieldType(typeName, name, "Rotation2d", node);
    } else {
      ConfigWarnings.warnMissingField(typeName, name, defaultValue);
    }
    return defaultValue;
  }

  public Rotation3d readRotation3d(String name, Rotation3d defaultValue) {
    ConfigNode node = fields.get(name);
    if (node instanceof Rotation3dNode rotationNode) {
      return rotationNode.getRotation();
    }
    if (node != null) {
      ConfigWarnings.warnWrongFieldType(typeName, name, "Rotation3d", node);
    } else {
      ConfigWarnings.warnMissingField(typeName, name, defaultValue);
    }
    return defaultValue;
  }

  public Translation2d readTranslation2d(String name, Translation2d defaultValue) {
    ConfigNode node = fields.get(name);
    if (node instanceof Translation2dNode translationNode) {
      return translationNode.getTranslation();
    }
    if (node != null) {
      ConfigWarnings.warnWrongFieldType(typeName, name, "Translation2d", node);
    } else {
      ConfigWarnings.warnMissingField(typeName, name, defaultValue);
    }
    return defaultValue;
  }

  public Translation3d readTranslation3d(String name, Translation3d defaultValue) {
    ConfigNode node = fields.get(name);
    if (node instanceof Translation3dNode translationNode) {
      return translationNode.getTranslation();
    }
    if (node != null) {
      ConfigWarnings.warnWrongFieldType(typeName, name, "Translation3d", node);
    } else {
      ConfigWarnings.warnMissingField(typeName, name, defaultValue);
    }
    return defaultValue;
  }

  public TrapezoidProfile.Constraints readTrapezoidConstraints(
      String name, TrapezoidProfile.Constraints defaultValue) {
    ConfigNode node = fields.get(name);
    if (node instanceof TrapezoidProfileConstraintsNode constraintsNode) {
      return constraintsNode.getConstraints();
    }
    if (node != null) {
      ConfigWarnings.warnWrongFieldType(typeName, name, "TrapezoidProfile.Constraints", node);
    } else {
      ConfigWarnings.warnMissingField(typeName, name, defaultValue);
    }
    return defaultValue;
  }

  public SwerveDriveKinematicsNode readSwerveKinematics(String name) {
    ConfigNode node = fields.get(name);
    if (node instanceof SwerveDriveKinematicsNode kinematicsNode) {
      return kinematicsNode;
    }
    if (node != null) {
      ConfigWarnings.warnWrongFieldType(typeName, name, "SwerveDriveKinematics", node);
    } else {
      ConfigWarnings.warnMissingField(typeName, name, "SwerveDriveKinematics");
    }
    return null;
  }

  public MecanumDriveKinematicsNode readMecanumKinematics(String name) {
    ConfigNode node = fields.get(name);
    if (node instanceof MecanumDriveKinematicsNode kinematicsNode) {
      return kinematicsNode;
    }
    if (node != null) {
      ConfigWarnings.warnWrongFieldType(typeName, name, "MecanumDriveKinematics", node);
    } else {
      ConfigWarnings.warnMissingField(typeName, name, "MecanumDriveKinematics");
    }
    return null;
  }

  public DifferentialDriveKinematicsNode readDifferentialKinematics(String name) {
    ConfigNode node = fields.get(name);
    if (node instanceof DifferentialDriveKinematicsNode kinematicsNode) {
      return kinematicsNode;
    }
    if (node != null) {
      ConfigWarnings.warnWrongFieldType(typeName, name, "DifferentialDriveKinematics", node);
    } else {
      ConfigWarnings.warnMissingField(typeName, name, "DifferentialDriveKinematics");
    }
    return null;
  }

  public Angle readAngle(String name, Angle defaultValue) {
    ConfigNode node = fields.get(name);
    if (node instanceof AngleNode angleNode) {
      return angleNode.getMeasure();
    }
    if (node != null) {
      ConfigWarnings.warnWrongFieldType(typeName, name, "Angle", node);
    } else {
      ConfigWarnings.warnMissingField(typeName, name, defaultValue);
    }
    return defaultValue;
  }

  public Distance readDistance(String name, Distance defaultValue) {
    ConfigNode node = fields.get(name);
    if (node instanceof DistanceNode distanceNode) {
      return distanceNode.getMeasure();
    }
    if (node != null) {
      ConfigWarnings.warnWrongFieldType(typeName, name, "Distance", node);
    } else {
      ConfigWarnings.warnMissingField(typeName, name, defaultValue);
    }
    return defaultValue;
  }
}
