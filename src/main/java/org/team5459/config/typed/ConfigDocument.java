package org.team5459.config.typed;

import static edu.wpi.first.units.Units.Hertz;
import static edu.wpi.first.units.Units.Kilograms;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.Seconds;
import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.DifferentialDriveFeedforward;
import edu.wpi.first.math.controller.ElevatorFeedforward;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Ellipse2d;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Quaternion;
import edu.wpi.first.math.geometry.Rectangle2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelPositions;
import edu.wpi.first.math.kinematics.MecanumDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.ExponentialProfile;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.constraint.DifferentialDriveKinematicsConstraint;
import edu.wpi.first.math.trajectory.constraint.MecanumDriveKinematicsConstraint;
import edu.wpi.first.math.trajectory.constraint.SwerveDriveKinematicsConstraint;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.Frequency;
import edu.wpi.first.units.measure.Mass;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.util.Color;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Loaded typed configuration document.
 *
 * <p>Use the explicit typed getters to retrieve live values from slash-separated paths such as
 * {@code Arm/PIDController}.
 */
public final class ConfigDocument {

  private final Map<String, ConfigNode> root;

  ConfigDocument(Map<String, ConfigNode> root) {
    this.root = Collections.unmodifiableMap(new LinkedHashMap<>(root));
  }

  /** Returns the node at the given path, or {@code null} if it does not exist. */
  public ConfigNode getNode(String path) {
    return ConfigPath.resolve(root, path);
  }

  public double getDouble(String path) {
    return getTypedNode(path, DoubleNode.class, DoubleNode::getValue, 0.0, "double");
  }

  public int getInt(String path) {
    return getTypedNode(path, IntNode.class, IntNode::getValue, 0, "int");
  }

  public boolean getBoolean(String path) {
    return getTypedNode(path, BooleanNode.class, BooleanNode::getValue, false, "boolean");
  }

  public String getString(String path) {
    return getTypedNode(path, StringNode.class, StringNode::getValue, "", "String");
  }

  public double[] getDoubleArray(String path) {
    return getTypedNode(
        path, DoubleArrayNode.class, DoubleArrayNode::getValue, new double[0], "double[]");
  }

  public int[] getIntArray(String path) {
    return getTypedNode(path, IntArrayNode.class, IntArrayNode::getValue, new int[0], "int[]");
  }

  public PIDController getPIDController(String path) {
    return getTypedNode(
        path,
        PIDControllerNode.class,
        PIDControllerNode::getController,
        new PIDController(0.0, 0.0, 0.0),
        "PIDController");
  }

  public ProfiledPIDController getProfiledPIDController(String path) {
    return getTypedNode(
        path,
        ProfiledPIDControllerNode.class,
        ProfiledPIDControllerNode::getController,
        new ProfiledPIDController(0.0, 0.0, 0.0, new TrapezoidProfile.Constraints(0.0, 0.0), 0.02),
        "ProfiledPIDController");
  }

  public Rotation2d getRotation2d(String path) {
    return getTypedNode(
        path, Rotation2dNode.class, Rotation2dNode::getRotation, new Rotation2d(), "Rotation2d");
  }

  public Rotation3d getRotation3d(String path) {
    return getTypedNode(
        path, Rotation3dNode.class, Rotation3dNode::getRotation, new Rotation3d(), "Rotation3d");
  }

  public Translation2d getTranslation2d(String path) {
    return getTypedNode(
        path,
        Translation2dNode.class,
        Translation2dNode::getTranslation,
        Translation2d.kZero,
        "Translation2d");
  }

  public Translation3d getTranslation3d(String path) {
    return getTypedNode(
        path,
        Translation3dNode.class,
        Translation3dNode::getTranslation,
        Translation3d.kZero,
        "Translation3d");
  }

  public Pose2d getPose2d(String path) {
    return getTypedNode(path, Pose2dNode.class, Pose2dNode::getPose, Pose2d.kZero, "Pose2d");
  }

  public Pose3d getPose3d(String path) {
    return getTypedNode(path, Pose3dNode.class, Pose3dNode::getPose, Pose3d.kZero, "Pose3d");
  }

  public Transform2d getTransform2d(String path) {
    return getTypedNode(
        path,
        Transform2dNode.class,
        Transform2dNode::getTransform,
        new Transform2d(),
        "Transform2d");
  }

  public Transform3d getTransform3d(String path) {
    return getTypedNode(
        path,
        Transform3dNode.class,
        Transform3dNode::getTransform,
        new Transform3d(),
        "Transform3d");
  }

  public Quaternion getQuaternion(String path) {
    return getTypedNode(
        path, QuaternionNode.class, QuaternionNode::getQuaternion, new Quaternion(), "Quaternion");
  }

  public Ellipse2d getEllipse2d(String path) {
    return getTypedNode(
        path,
        Ellipse2dNode.class,
        Ellipse2dNode::getEllipse,
        new Ellipse2d(Pose2d.kZero, 0.0, 0.0),
        "Ellipse2d");
  }

  public Rectangle2d getRectangle2d(String path) {
    return getTypedNode(
        path,
        Rectangle2dNode.class,
        Rectangle2dNode::getRectangle,
        new Rectangle2d(Pose2d.kZero, 0.0, 0.0),
        "Rectangle2d");
  }

  public Angle getAngle(String path) {
    return getTypedNode(path, AngleNode.class, AngleNode::getMeasure, Radians.of(0.0), "Angle");
  }

  public Distance getDistance(String path) {
    return getTypedNode(
        path, DistanceNode.class, DistanceNode::getMeasure, Meters.of(0.0), "Distance");
  }

  public Time getTime(String path) {
    return getTypedNode(path, TimeNode.class, TimeNode::getMeasure, Seconds.of(0.0), "Time");
  }

  public Frequency getFrequency(String path) {
    return getTypedNode(
        path, FrequencyNode.class, FrequencyNode::getMeasure, Hertz.of(0.0), "Frequency");
  }

  public Mass getMass(String path) {
    return getTypedNode(path, MassNode.class, MassNode::getMeasure, Kilograms.of(0.0), "Mass");
  }

  public Voltage getVoltage(String path) {
    return getTypedNode(path, VoltageNode.class, VoltageNode::getMeasure, Volts.of(0.0), "Voltage");
  }

  public Color getColor(String path) {
    return getTypedNode(path, ColorNode.class, ColorNode::getColor, new Color(0, 0, 0), "Color");
  }

  public AprilTagFieldLayout getAprilTagFieldLayout(String path) {
    return getTypedNode(
        path,
        AprilTagFieldLayoutNode.class,
        AprilTagFieldLayoutNode::getLayout,
        AprilTagFieldLayout.loadField(AprilTagFields.kDefaultField),
        "AprilTagFieldLayout");
  }

  public SimpleMotorFeedforward getSimpleMotorFeedforward(String path) {
    return getTypedNode(
        path,
        SimpleMotorFeedforwardNode.class,
        SimpleMotorFeedforwardNode::getFeedforward,
        new SimpleMotorFeedforward(0.0, 0.0),
        "SimpleMotorFeedforward");
  }

  public ArmFeedforward getArmFeedforward(String path) {
    return getTypedNode(
        path,
        ArmFeedforwardNode.class,
        ArmFeedforwardNode::getFeedforward,
        new ArmFeedforward(0.0, 0.0, 0.0),
        "ArmFeedforward");
  }

  public ElevatorFeedforward getElevatorFeedforward(String path) {
    return getTypedNode(
        path,
        ElevatorFeedforwardNode.class,
        ElevatorFeedforwardNode::getFeedforward,
        new ElevatorFeedforward(0.0, 0.0, 0.0),
        "ElevatorFeedforward");
  }

  public DifferentialDriveFeedforward getDifferentialDriveFeedforward(String path) {
    return getTypedNode(
        path,
        DifferentialDriveFeedforwardNode.class,
        DifferentialDriveFeedforwardNode::getFeedforward,
        new DifferentialDriveFeedforward(0.0, 0.0, 0.0, 0.0),
        "DifferentialDriveFeedforward");
  }

  public DifferentialDriveKinematics getDifferentialDriveKinematics(String path) {
    return getTypedNode(
        path,
        DifferentialDriveKinematicsNode.class,
        DifferentialDriveKinematicsNode::getKinematics,
        new DifferentialDriveKinematics(0.0),
        "DifferentialDriveKinematics");
  }

  public MecanumDriveKinematics getMecanumDriveKinematics(String path) {
    return getTypedNode(
        path,
        MecanumDriveKinematicsNode.class,
        MecanumDriveKinematicsNode::getKinematics,
        new MecanumDriveKinematics(
            Translation2d.kZero, Translation2d.kZero, Translation2d.kZero, Translation2d.kZero),
        "MecanumDriveKinematics");
  }

  public SwerveDriveKinematics getSwerveDriveKinematics(String path) {
    return getTypedNode(
        path,
        SwerveDriveKinematicsNode.class,
        SwerveDriveKinematicsNode::getKinematics,
        new SwerveDriveKinematics(
            Translation2d.kZero, Translation2d.kZero, Translation2d.kZero, Translation2d.kZero),
        "SwerveDriveKinematics");
  }

  public DifferentialDriveKinematicsConstraint getDifferentialDriveKinematicsConstraint(
      String path) {
    return getTypedNode(
        path,
        DifferentialDriveKinematicsConstraintNode.class,
        DifferentialDriveKinematicsConstraintNode::getConstraint,
        new DifferentialDriveKinematicsConstraint(new DifferentialDriveKinematics(0.0), 0.0),
        "DifferentialDriveKinematicsConstraint");
  }

  public MecanumDriveKinematicsConstraint getMecanumDriveKinematicsConstraint(String path) {
    return getTypedNode(
        path,
        MecanumDriveKinematicsConstraintNode.class,
        MecanumDriveKinematicsConstraintNode::getConstraint,
        new MecanumDriveKinematicsConstraint(
            new MecanumDriveKinematics(
                Translation2d.kZero, Translation2d.kZero, Translation2d.kZero, Translation2d.kZero),
            0.0),
        "MecanumDriveKinematicsConstraint");
  }

  public SwerveDriveKinematicsConstraint getSwerveDriveKinematicsConstraint(String path) {
    return getTypedNode(
        path,
        SwerveDriveKinematicsConstraintNode.class,
        SwerveDriveKinematicsConstraintNode::getConstraint,
        new SwerveDriveKinematicsConstraint(
            new SwerveDriveKinematics(
                Translation2d.kZero, Translation2d.kZero, Translation2d.kZero, Translation2d.kZero),
            0.0),
        "SwerveDriveKinematicsConstraint");
  }

  public DifferentialDriveWheelPositions getDifferentialDriveWheelPositions(String path) {
    return getTypedNode(
        path,
        DifferentialDriveWheelPositionsNode.class,
        DifferentialDriveWheelPositionsNode::getWheelPositions,
        new DifferentialDriveWheelPositions(0.0, 0.0),
        "DifferentialDriveWheelPositions");
  }

  public MecanumDrive.WheelSpeeds getMecanumDriveWheelSpeeds(String path) {
    return getTypedNode(
        path,
        MecanumDriveWheelSpeedsNode.class,
        MecanumDriveWheelSpeedsNode::getWheelSpeeds,
        new MecanumDrive.WheelSpeeds(),
        "MecanumDrive.WheelSpeeds");
  }

  public SwerveModulePosition getSwerveModulePosition(String path) {
    return getTypedNode(
        path,
        SwerveModulePositionNode.class,
        SwerveModulePositionNode::getModulePosition,
        new SwerveModulePosition(),
        "SwerveModulePosition");
  }

  public SwerveModuleState getSwerveModuleState(String path) {
    return getTypedNode(
        path,
        SwerveModuleStateNode.class,
        SwerveModuleStateNode::getModuleState,
        new SwerveModuleState(0.0, new Rotation2d()),
        "SwerveModuleState");
  }

  public TrapezoidProfile.Constraints getTrapezoidProfileConstraints(String path) {
    return getTypedNode(
        path,
        TrapezoidProfileConstraintsNode.class,
        TrapezoidProfileConstraintsNode::getConstraints,
        new TrapezoidProfile.Constraints(0.0, 0.0),
        "TrapezoidProfile.Constraints");
  }

  public TrapezoidProfile.State getTrapezoidProfileState(String path) {
    return getTypedNode(
        path,
        TrapezoidProfileStateNode.class,
        TrapezoidProfileStateNode::getState,
        new TrapezoidProfile.State(0.0, 0.0),
        "TrapezoidProfile.State");
  }

  public ExponentialProfile.Constraints getExponentialProfileConstraints(String path) {
    return getTypedNode(
        path,
        ExponentialProfileConstraintsNode.class,
        ExponentialProfileConstraintsNode::getConstraints,
        ExponentialProfile.Constraints.fromCharacteristics(0.0, 0.0, 0.0),
        "ExponentialProfile.Constraints");
  }

  public ExponentialProfile.State getExponentialProfileState(String path) {
    return getTypedNode(
        path,
        ExponentialProfileStateNode.class,
        ExponentialProfileStateNode::getState,
        new ExponentialProfile.State(0.0, 0.0),
        "ExponentialProfile.State");
  }

  public Trajectory.State getTrajectoryState(String path) {
    return getTypedNode(
        path,
        TrajectoryStateNode.class,
        TrajectoryStateNode::getState,
        new Trajectory.State(0.0, 0.0, 0.0, Pose2d.kZero, 0.0),
        "Trajectory.State");
  }

  Map<String, ConfigNode> getRootEntries() {
    return root;
  }

  private <T, N extends ConfigNode> T getTypedNode(
      String path, Class<N> nodeType, Function<N, T> extractor, T defaultValue, String typeName) {
    ConfigNode node = getNode(path);
    if (nodeType.isInstance(node)) {
      return extractor.apply(nodeType.cast(node));
    }
    if (node != null) {
      ConfigWarnings.warnTypeMismatch(path, typeName, node);
    }
    return defaultValue;
  }
}
