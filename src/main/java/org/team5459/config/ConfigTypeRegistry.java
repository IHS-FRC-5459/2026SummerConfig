package org.team5459.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.team5459.config.types.*;

/**
 * Maps JSON {@code type} discriminators to concrete {@link ConfigNode} classes for Jackson.
 *
 * <p>Every supported type string and its node class must be listed in {@link #NAMED_TYPES}. Adding
 * a new config type requires:
 *
 * <ol>
 *   <li>Implement the node under {@link org.team5459.config.types}.
 *   <li>Register it here with the exact JSON {@code type} string.
 *   <li>Add a typed getter to {@link ConfigDocument} if subsystems should read it by path.
 *   <li>Extend {@link ConfigJsonWriter} and {@link TypedNetworkTableSync} if the type is a new
 *       leaf scalar/array shape.
 * </ol>
 */
public final class ConfigTypeRegistry {

  private ConfigTypeRegistry() {}

  static void registerSubtypes(ObjectMapper mapper) {
    for (NamedType type : NAMED_TYPES) {
      mapper.registerSubtypes(type);
    }
  }

  static String typeNameFor(Class<? extends ConfigNode> nodeClass) {
    String typeName = TYPE_NAME_BY_CLASS.get(nodeClass);
    if (typeName == null) {
      throw new IllegalArgumentException("Unsupported config node class: " + nodeClass.getName());
    }
    return typeName;
  }

  /** Supported JSON {@code type} strings that can appear in config files. */
  public static List<String> supportedTypeNames() {
    return NAMED_TYPES.stream().map(NamedType::getName).sorted().toList();
  }

  private static final List<NamedType> NAMED_TYPES =
      List.of(
          new NamedType(FolderNode.class, "folder"),
          new NamedType(DoubleNode.class, "double"),
          new NamedType(IntNode.class, "int"),
          new NamedType(BooleanNode.class, "boolean"),
          new NamedType(StringNode.class, "String"),
          new NamedType(DoubleArrayNode.class, "double[]"),
          new NamedType(IntArrayNode.class, "int[]"),
          new NamedType(PIDControllerNode.class, "PIDController"),
          new NamedType(ProfiledPIDControllerNode.class, "ProfiledPIDController"),
          new NamedType(Translation2dNode.class, "Translation2d"),
          new NamedType(Translation3dNode.class, "Translation3d"),
          new NamedType(Rotation2dNode.class, "Rotation2d"),
          new NamedType(Rotation3dNode.class, "Rotation3d"),
          new NamedType(Pose2dNode.class, "Pose2d"),
          new NamedType(Pose3dNode.class, "Pose3d"),
          new NamedType(Transform2dNode.class, "Transform2d"),
          new NamedType(Transform3dNode.class, "Transform3d"),
          new NamedType(QuaternionNode.class, "Quaternion"),
          new NamedType(Ellipse2dNode.class, "Ellipse2d"),
          new NamedType(Rectangle2dNode.class, "Rectangle2d"),
          new NamedType(AngleNode.class, "Angle"),
          new NamedType(DistanceNode.class, "Distance"),
          new NamedType(TimeNode.class, "Time"),
          new NamedType(FrequencyNode.class, "Frequency"),
          new NamedType(MassNode.class, "Mass"),
          new NamedType(VoltageNode.class, "Voltage"),
          new NamedType(ColorNode.class, "Color"),
          new NamedType(AprilTagFieldLayoutNode.class, "AprilTagFieldLayout"),
          new NamedType(SimpleMotorFeedforwardNode.class, "SimpleMotorFeedforward"),
          new NamedType(ArmFeedforwardNode.class, "ArmFeedforward"),
          new NamedType(ElevatorFeedforwardNode.class, "ElevatorFeedforward"),
          new NamedType(DifferentialDriveFeedforwardNode.class, "DifferentialDriveFeedforward"),
          new NamedType(DifferentialDriveKinematicsNode.class, "DifferentialDriveKinematics"),
          new NamedType(MecanumDriveKinematicsNode.class, "MecanumDriveKinematics"),
          new NamedType(SwerveDriveKinematicsNode.class, "SwerveDriveKinematics"),
          new NamedType(
              DifferentialDriveKinematicsConstraintNode.class,
              "DifferentialDriveKinematicsConstraint"),
          new NamedType(
              MecanumDriveKinematicsConstraintNode.class, "MecanumDriveKinematicsConstraint"),
          new NamedType(
              SwerveDriveKinematicsConstraintNode.class, "SwerveDriveKinematicsConstraint"),
          new NamedType(
              DifferentialDriveWheelPositionsNode.class, "DifferentialDriveWheelPositions"),
          new NamedType(MecanumDriveWheelSpeedsNode.class, "MecanumDrive.WheelSpeeds"),
          new NamedType(SwerveModulePositionNode.class, "SwerveModulePosition"),
          new NamedType(SwerveModuleStateNode.class, "SwerveModuleState"),
          new NamedType(TrapezoidProfileConstraintsNode.class, "TrapezoidProfile.Constraints"),
          new NamedType(TrapezoidProfileStateNode.class, "TrapezoidProfile.State"),
          new NamedType(ExponentialProfileConstraintsNode.class, "ExponentialProfile.Constraints"),
          new NamedType(ExponentialProfileStateNode.class, "ExponentialProfile.State"),
          new NamedType(TrajectoryStateNode.class, "Trajectory.State"));

  private static final Map<Class<? extends ConfigNode>, String> TYPE_NAME_BY_CLASS =
      buildTypeNameByClass();

  private static Map<Class<? extends ConfigNode>, String> buildTypeNameByClass() {
    Map<Class<? extends ConfigNode>, String> typeNames = new LinkedHashMap<>();
    for (NamedType namedType : NAMED_TYPES) {
      @SuppressWarnings("unchecked")
      Class<? extends ConfigNode> nodeClass = (Class<? extends ConfigNode>) namedType.getType();
      typeNames.put(nodeClass, namedType.getName());
    }
    return Map.copyOf(typeNames);
  }
}
