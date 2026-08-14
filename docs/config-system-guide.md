# org.team5459.config — Developer Guide

A typed JSON configuration system for FRC robots. This guide explains how the library works, how each major file fits together, and how errors and warnings are handled.

---

## Table of Contents

1. [Overview](#overview)
2. [Libraries Used](#libraries-used)
3. [JSON Format](#json-format)
4. [The Node Tree (Core Abstractions)](#the-node-tree-core-abstractions)
5. [File-by-File Breakdown (Main Package)](#file-by-file-breakdown-main-package)
6. [The types/ Package](#the-types-package)
7. [Deep Dive: DoubleNode (Scalar Leaf)](#deep-dive-doublenode-scalar-leaf)
8. [Deep Dive: PIDControllerNode (Live Composite)](#deep-dive-pidcontrollernode-live-composite)
9. [Deep Dive: Pose2dNode (Immutable Composite)](#deep-dive-pose2dnode-immutable-composite)
10. [Error and Warning Handling](#error-and-warning-handling)
11. [Quick Reference](#quick-reference)

---

## Overview

This library lets you:

1. Store robot settings (PID gains, poses, feedforwards, etc.) in a **JSON file** (defaults only)
2. Load them at startup into a typed **`ConfigDocument`**
3. Read values from robot code via **typed getters** (e.g. `getPIDController("Arm/PIDController")`)
4. Publish values to **NetworkTables** (`/Config/...`) for dashboards (e.g. Elastic)
5. In **debug** mode only: accept NT edits, autosave `config-cache.json`, and **promote** to `robot-config.json` via Save or Save As file watch

### Modes

| Mode | When | Getters | NT edits | JSON writes |
|------|------|---------|----------|-------------|
| **Debug** | `!FMS` and `Config/DebugMode` | Live NT-backed document | Applied + cache autosave | Promote via `Config/Save` or `elastic-layout.json` content change |
| **Match** | FMS attached, or DebugMode off | `robot-config.json` defaults | Ignored (publish only) | None |

JSON is only for applying defaults at startup (and after promote). Live tuning is through NT in debug. Ship committed defaults with a normal code deploy — no SSH push required.

### Typical Workflow

```
robot-config.json  →  defaults document
       ↓
ConfigManager.publish → /Config/...
       ↓
Debug: listen + cache autosave; getters → live/NT
Match: ignore NT writebacks; getters → JSON defaults
       ↓
Promote (debug): Config/Save OR elastic-layout.json changed → robot-config.json
```

### Typical Robot Code

```java
// In Robot.robotInit()
configManager = new ConfigManager(
    new File(Filesystem.getDeployDirectory(), "robot-config.json"),
    new File(Filesystem.getDeployDirectory(), "config-cache.json"));

// In Robot.robotPeriodic()
configManager.periodic();

// In a subsystem constructor
PIDController armPid = configManager.getDocument().getPIDController("Arm/PIDController");

// In periodic()
double output = armPid.calculate(setpoint, measurement);
```

---

## Libraries Used

| Library | What it does here |
|---------|-------------------|
| **Jackson** (`com.fasterxml.jackson`) | Parses JSON into Java objects. Uses `@JsonCreator`, `@JsonProperty`, and `@JsonTypeInfo` for polymorphic deserialization (picking the right `ConfigNode` subclass from the `"type"` field). |
| **WPILib NetworkTables** (`edu.wpi.first.networktables`) | Publishes config values so dashboards can read/write them. |
| **WPILib math/units** (`edu.wpi.first.math.*`, `edu.wpi.first.units.*`) | The robot types this config wraps: `PIDController`, `Pose2d`, `Distance`, etc. |

---

## JSON Format

Every config entry uses the same envelope:

```json
{
  "Arm": {
    "type": "folder",
    "value": {
      "PIDController": {
        "type": "PIDController",
        "value": {
          "p": { "type": "double", "value": 0.1 },
          "i": { "type": "double", "value": 0.01 },
          "d": { "type": "double", "value": 0.001 }
        }
      }
    }
  }
}
```

- **`"type"`** — selects which Java class to use (registered in `ConfigTypeRegistry`)
- **`"value"`** — either a primitive (`0.1`), an array, or a nested object of child nodes

Paths like `Arm/PIDController/p` mirror the JSON folder structure.

---

## The Node Tree (Core Abstractions)

### ConfigNode — base of everything

| Method | What it does |
|--------|--------------|
| `initialize()` | Hook called after JSON load. Default is no-op; subclasses build runtime state here. |
| `getChildEntries()` | Returns named children for path lookup, or `null` if this node has no children. |
| `initializeTree(node)` (static) | Walks the whole tree and calls `initialize()` on every node. |

Jackson reads the `"type"` field to pick the subclass and ignores extra JSON properties.

### CompositeConfigNode — nodes made of child fields

Examples: `PIDController`, `Pose2d`, `SimpleMotorFeedforward`.

| Method | What it does |
|--------|--------------|
| Constructor | Takes the `"value"` map of named child `ConfigNode`s. |
| `getFields()` | Returns the child map (read-only). |
| `getChildEntries()` | Same map — enables path traversal into children. |
| `initialize()` | Initializes all children, then calls `syncValue()`. |
| `syncValue()` (abstract) | Rebuilds runtime state from current child values. |
| `applyFieldChanges()` | Called when a child is edited over NetworkTables; re-runs `syncValue()`. |
| `reader(typeName)` | Creates a `ConfigFieldReader` for safely reading typed children. |

### ValueConfigNode&lt;T&gt; — composite that caches an immutable value

Examples: `Pose2dNode`, `Translation2dNode`, feedforward nodes.

| Method | What it does |
|--------|--------------|
| `getValue()` | Returns the cached `T` (e.g. a `Pose2d`). |
| `syncValue()` | Calls `buildValue()` and stores the result. |
| `buildValue()` (abstract) | Constructs the WPILib object from child fields. |
| `typeName()` (abstract) | Name used in warning messages. |
| `reader()` | Shortcut for `reader(typeName())`. |

Each sync produces a **fresh value object**. When a child scalar changes over NetworkTables, the parent is rebuilt automatically.

---

## File-by-File Breakdown (Main Package)

### ConfigPath — path resolution

| Method | What it does |
|--------|--------------|
| `resolve(root, path)` | Splits `"Arm/PIDController/p"` on `/`, walks folder/composite child maps. Returns the target node, or `null` with a warning if anything is missing or not navigable. |

### ConfigTypeRegistry — Jackson type mapping

| Method | What it does |
|--------|--------------|
| `registerSubtypes(mapper)` | Registers all `NamedType` entries so Jackson knows `"double"` → `DoubleNode`, etc. |
| `typeNameFor(nodeClass)` | Reverse lookup: class → JSON type string (used when saving). |
| `supportedTypeNames()` | Returns sorted list of all valid `"type"` strings. |

Adding a new config type requires: implement the node, register it here, add a getter to `ConfigDocument`, and extend `ConfigJsonWriter` / `TypedNetworkTableSync` if it's a new scalar shape.

### TypedConfigMapper — shared Jackson setup

| Method | What it does |
|--------|--------------|
| `mapper()` | Returns a singleton `ObjectMapper` configured to only use `@JsonCreator` constructors, ignore unknown properties, and register all config subtypes. |

### TypedConfigLoader — load JSON from disk

| Method | What it does |
|--------|--------------|
| `load(jsonFile)` | Parses the file into `Map<String, ConfigNode>`, runs `initializeTree` on every root entry, wraps in a `ConfigDocument`. Throws `UncheckedIOException` on failure. |

### TypedConfigSaver — save to disk

| Method | What it does |
|--------|--------------|
| `save(jsonFile, document)` | Writes via `ConfigJsonWriter` (not Jackson), so only `type` + `value` appear in the file. |

### ConfigJsonWriter — explicit JSON output

| Method | What it does |
|--------|--------------|
| `write(file, rootEntries)` | Writes the root object with pretty printing. |
| `writeNode(node, generator)` | Writes `{ "type": "...", "value": ... }` for one node. |
| `writeValue(node, generator)` | Dispatches: folders/composites → nested objects; scalars → numbers/booleans/strings/arrays. |

### ConfigFieldReader — safe child field access for composites

Used inside `buildValue()` / `syncValue()`. Every `read*` method:

- Child exists and is the right type → return its value
- Child exists but wrong type → warn, return default
- Child missing → warn, return default

Methods include: `readDouble`, `readInt`, `readBoolean`, `readString`, `readRotation2d`, `readRotation3d`, `readTranslation2d`, `readTranslation3d`, `readTrapezoidConstraints`, `readSwerveKinematics`, `readMecanumKinematics`, `readDifferentialKinematics`, `readAngle`, `readDistance`.

### ConfigDocument — the API robot code uses

| Method | What it does |
|--------|--------------|
| `getNode(path)` | Raw node lookup via `ConfigPath`. |
| `getDouble(path)`, `getInt(path)`, `getBoolean(path)`, etc. | ~40 typed getters. Each resolves the path, checks the node class, extracts the value, or returns a safe default with a warning. |
| `getRootEntries()` | Package-private; used by save/sync. |
| `getTypedNode(...)` (private) | Shared implementation for all typed getters. |

### ConfigWarnings — log, don't crash

All methods print to `System.err` with a `[Config]` prefix. See [Error and Warning Handling](#error-and-warning-handling).

### TypedNetworkTableSync — live tuning bridge

| Method | What it does |
|--------|--------------|
| `publish(document)` | Publishes the whole tree under `/Config`. Folders/composites become subtables; scalars become NT entries. |
| `listen(document)` / `listen(document, onUpdate)` | Attaches listeners on editable scalar leaves. Dashboard writes update in-memory nodes, call `applyFieldChanges()` on parents, then run `onUpdate` (typically autosave). |

Only **scalars and arrays** are directly editable over NetworkTables.

### ConfigSaveButton — commit button

| Method | What it does |
|--------|--------------|
| `listen(configFile, document)` | Publishes `ConfigManager/Save` as a boolean entry. |
| Listener callback | When entry goes `true`, saves to the real config file and resets to `false`. |

### ConfigManager — wires everything together

| Method | What it does |
|--------|--------------|
| Constructor | Loads config, publishes to NT, sets up autosave-to-cache listeners, sets up Save button. |
| `getDocument()` | Returns the live `ConfigDocument`. |
| `getConfigFile()` / `getCacheFile()` | File references. |
| `close()` | Closes all NT listeners. |

---

## The types/ Package

There are ~45 type files in three categories:

### 1. Scalar/array leaves

`DoubleNode`, `IntNode`, `BooleanNode`, `StringNode`, `DoubleArrayNode`, `IntArrayNode`

Store a primitive, have `getValue()` / `setValue()`, sync directly to NetworkTables.

### 2. Immutable composites (ValueConfigNode&lt;T&gt;)

`Pose2dNode`, `Rotation2dNode`, `SimpleMotorFeedforwardNode`, `DistanceNode`, etc.

Implement `buildValue()` to construct a WPILib object from child fields. Value is rebuilt when children change.

### 3. Live composites (CompositeConfigNode directly)

`PIDControllerNode`, `ProfiledPIDControllerNode`

Keep **one mutable runtime object** for the lifetime of the document. `syncValue()` updates that object in place.

### 4. Organizational

`FolderNode` — pure grouping, no runtime state.

Every class must be registered in `ConfigTypeRegistry`.

---

## Deep Dive: DoubleNode (Scalar Leaf)

```java
public final class DoubleNode extends ConfigNode {
  private double value;

  @JsonCreator
  public DoubleNode(@JsonProperty("value") double value) {
    this.value = value;
  }

  @JsonProperty("value")
  public double getValue() { return value; }

  public void setValue(double value) { this.value = value; }
}
```

| Piece | Purpose |
|-------|---------|
| `@JsonCreator` on constructor | Jackson uses this when deserializing. |
| `@JsonProperty("value")` | Maps JSON `"value": 0.1` to the constructor/getter. |
| `getValue()` | Read by `ConfigDocument.getDouble()` and `ConfigFieldReader`. |
| `setValue()` | Called by `TypedNetworkTableSync` when a dashboard edits the value. |
| No `getChildEntries()` | Leaf node — path traversal stops here. |
| No `initialize()` override | Nothing to set up beyond storing the number. |

On disk: `{ "type": "double", "value": 0.1 }`
On NetworkTables: `/Config/Arm/PIDController/p = 0.1`

Every other scalar leaf (`IntNode`, `BooleanNode`, etc.) follows the same pattern.

---

## Deep Dive: PIDControllerNode (Live Composite)

### Why live?

Subsystems hold a reference and call `.calculate()` every loop. If tuning changed which object you got back, the subsystem would silently keep using stale gains. `PIDControllerNode` keeps one instance and updates it in place.

### JSON shape

```json
"PIDController": {
  "type": "PIDController",
  "value": {
    "p": { "type": "double", "value": 0.1 },
    "i": { "type": "double", "value": 0.01 },
    "d": { "type": "double", "value": 0.001 }
  }
}
```

### Key code

```java
public final class PIDControllerNode extends CompositeConfigNode {
  private final PIDController controller = new PIDController(0.0, 0.0, 0.0);

  @Override
  protected void syncValue() {
    ConfigFieldReader fieldReader = reader("PIDController");
    controller.setPID(
        fieldReader.readDouble("p", 0.0),
        fieldReader.readDouble("i", 0.0),
        fieldReader.readDouble("d", 0.0));
  }
}
```

| Piece | What it does |
|-------|--------------|
| `controller` field | Created **once**. Never replaced. |
| `@JsonIgnore` on `getController()` | Prevents Jackson from serializing the live WPILib object. |
| `syncValue()` | Reads p/i/d from child nodes, calls `controller.setPID(...)`. |

### Lifecycle: load

1. Jackson creates `PIDControllerNode` with child `DoubleNode`s
2. `initialize()` initializes children, then `syncValue()` sets gains on the controller

### Lifecycle: live tuning from Elastic

1. Dashboard sets `/Config/Arm/PIDController/p = 0.5`
2. `TypedNetworkTableSync` calls `DoubleNode.setValue(0.5)`
3. Parent `PIDControllerNode.applyFieldChanges()` → `syncValue()` → `controller.setPID(0.5, ...)`
4. Subsystem's existing reference sees new gains immediately

### Robot code pattern

```java
// Fetch once, use forever
private final PIDController armPid;

public ArmSubsystem(ConfigManager config) {
  armPid = config.getDocument().getPIDController("Arm/PIDController");
}

public void periodic() {
  double output = armPid.calculate(setpoint, getMeasurement());
}
```

---

## Deep Dive: Pose2dNode (Immutable Composite)

### Why immutable?

`Pose2d` is a value object — you read it, you don't mutate it. The config system rebuilds a fresh `Pose2d` whenever underlying fields change.

### JSON — flexible field layouts

**Inline x/y/deg:**

```json
"StartPose": {
  "type": "Pose2d",
  "value": {
    "x": { "type": "double", "value": 3.0 },
    "y": { "type": "double", "value": 1.5 },
    "deg": { "type": "double", "value": 90.0 }
  }
}
```

**Nested translation + rotation:**

```json
"value": {
  "translation": {
    "type": "Translation2d",
    "value": {
      "x": { "type": "double", "value": 3.0 },
      "y": { "type": "double", "value": 1.5 }
    }
  },
  "rotation": {
    "type": "Rotation2d",
    "value": { "deg": { "type": "double", "value": 90.0 } }
  }
}
```

Rotation priority: `rotation` → `deg` → `heading` → default zero.

### Inheritance chain

```
ConfigNode
  └── CompositeConfigNode
        └── ValueConfigNode<Pose2d>
              └── Pose2dNode
```

`Pose2dNode` only implements `buildValue()` — `ValueConfigNode` handles caching via `syncValue()`.

### Key code

```java
@Override
protected Pose2d buildValue() {
  ConfigFieldReader fieldReader = reader();
  Translation2d translation;
  if (getFields().containsKey("translation")) {
    translation = fieldReader.readTranslation2d("translation", Translation2d.kZero);
  } else {
    translation = new Translation2d(
        fieldReader.readDouble("x", 0.0),
        fieldReader.readDouble("y", 0.0));
  }
  // ... rotation logic ...
  return new Pose2d(translation, rotation);
}
```

### Live tuning

1. Dashboard edits `/Config/Robot/StartPose/x`
2. `DoubleNode.setValue(newX)` → `Pose2dNode.applyFieldChanges()` → new `Pose2d` cached
3. Next `document.getPose2d("Robot/StartPose")` returns the updated pose

Unlike PID, you don't need to hold a reference — call the getter when you need the current value.

### Robot code pattern

```java
public void resetOdometry(ConfigDocument config) {
  Pose2d start = config.getPose2d("Robot/StartPose");
  odometry.reset(start);
}
```

---

## Live vs Immutable — Comparison

| | PIDControllerNode (live) | Pose2dNode (immutable) |
|---|---|---|
| Extends | `CompositeConfigNode` | `ValueConfigNode<Pose2d>` |
| Runtime object | One `PIDController`, created once | New `Pose2d` on every sync |
| `syncValue()` | Updates existing controller in place | Calls `buildValue()`, stores result |
| Subsystem pattern | Fetch once, hold reference | Read getter when needed |
| When child changes | Same object, new gains | New object in cache |

### Which pattern to use when adding a new type

**Live composite** — mutable WPILib objects used across loops (`PIDController`, `ProfiledPIDController`).

**Immutable composite** — value objects (`Pose2d`, `Translation2d`, `SimpleMotorFeedforward`, unit measures).

**Scalar leaf** — single primitive with no sub-fields (`DoubleNode`).

---

## Error and Warning Handling

The system uses two strategies:

### Warn and continue (robot keeps running)

Most problems go through **`ConfigWarnings`**, printing to **`System.err`** with a **`[Config]`** prefix and returning a safe default.

| Problem | What happens |
|---------|--------------|
| Bad/missing path | Warn → return default |
| Wrong getter for a path (e.g. `getInt` on a double) | Warn → return that getter's default |
| Path goes too deep through a leaf | Warn → return default |
| Composite missing a child field | Warn at load/tune time → use field default |
| Composite child has wrong type | Warn → use default |
| Node can't sync to NetworkTables | Warn → skip that entry |

**Goal:** Typos in config should not crash the robot.

### Throw and stop (file/system is broken)

| Problem | Result |
|---------|--------|
| Config file missing, unreadable, or invalid JSON | `UncheckedIOException` on load |
| Unknown `"type"` in JSON | Load fails |
| Can't write on save | `UncheckedIOException` |
| Unregistered node class when saving | `IllegalArgumentException` |

**Goal:** If the whole file is unusable, fail loudly.

### Warning types

| Warning | When |
|---------|------|
| `warnMissingPath` | Path null, blank, or segment doesn't exist |
| `warnTypeMismatch` | Path exists but wrong getter type |
| `warnNotNavigable` | Tried to go deeper through a leaf node |
| `warnMissingField` | Composite missing expected child |
| `warnWrongFieldType` | Composite child has wrong type |
| `warnUnsupportedNetworkTablesType` | Node can't be published/listened on NT |

### Safe defaults (ConfigDocument getters)

| Getter | Default on failure |
|--------|-------------------|
| `getDouble` | `0.0` |
| `getInt` | `0` |
| `getBoolean` | `false` |
| `getString` | `""` |
| `getDoubleArray` | `new double[0]` |
| `getIntArray` | `new int[0]` |
| `getPIDController` | `new PIDController(0, 0, 0)` *(new object, not the config one)* |
| `getPose2d` | `Pose2d.kZero` |
| ... | Similar for all typed getters |

**Important:** If `"Arm/PIDController"` is **missing**, `getPIDController` returns a brand-new default controller — not the one in your config tree.

### What does NOT warn or throw

- Extra JSON properties → **silently ignored**
- NetworkTables listener receives null value → **silent return**
- ConfigManager autosave/save success → `System.out.println` (info only)

### Debugging

Look for **`[Config]`** in Driver Station console output.

| Symptom | Likely cause |
|---------|--------------|
| PID gains seem like 0 | Missing path or missing field |
| Getter returns zero but JSON looks fine | Wrong getter type |
| Can't tune in Elastic | Only scalars/arrays are NT-editable |
| Robot crashes on startup | Load threw — fix JSON syntax or unknown `"type"` |

---

## Quick Reference

### Mental model

```
JSON file
  └── Map<String, ConfigNode>  (root)
        └── FolderNode / CompositeConfigNode / leaf nodes
              └── each has { type, value }

ConfigDocument          → typed getters for robot code
TypedNetworkTableSync   → mirror to /Config, listen for edits
TypedConfigSaver        → write back to JSON
ConfigManager           → orchestrates all of the above
```

### NetworkTables layout example

```
/Config/Arm/PIDController/p    = 0.1
/Config/Arm/PIDController/i    = 0.01
/Config/Arm/PIDController/d    = 0.001
/Config/Robot/StartPose/x      = 3.0
/Config/Robot/StartPose/y      = 1.5
/Config/Robot/StartPose/deg    = 90.0
/ConfigManager/Save            = false (momentary button)
```

### Cache vs commit

| File | Purpose |
|------|---------|
| `robot-config.json` | Committed config — loaded at startup, written on Save |
| `config-cache.json` | Scratch file — live tuning autosaves here immediately |

---

*Generated from team documentation session. Source: `org.team5459.config` package in the Config project.*
