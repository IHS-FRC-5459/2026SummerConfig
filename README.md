# Config

FRC robot project with a typed JSON configuration library (`org.team5459.config`).

Robot settings live in `src/main/deploy/robot-config.json`. `ConfigManager` publishes them to NetworkTables under `/Config`.

- **Debug** (`!FMS` and `Config/DebugMode`): getters read live NT-backed values; edits autosave to `config-cache.json`; promote to `robot-config.json` with `Config/Save` or when `elastic-layout.json` content changes (Save As).
- **Match** (FMS, or DebugMode off): getters read JSON defaults; NT is display-only; no JSON writes. Ship updated defaults via normal deploy.

## Creating constants (all supported)

1. **JSON / code** — add entries to `robot-config.json` (typed schema) and deploy.
2. **Create panel** (debug) — place Elastic widgets for:
   - ComboBox Chooser → `/Config/Create/Type`
   - Text Display → `/Config/Create/Path` (e.g. `Intake/ArmPID`)
   - Toggle Switch → `/Config/Create/Go`
   Pick a type, enter a path, flip Go. The robot clones the matching `templates/template*` entry, autosaves the cache, then clears Path / resets type / sets Go false.
3. **Elastic Custom** — create a scalar, rebind topic to `/Config/...` (debug auto-register). Templates remain published for defaults/cloning.

Sim cache writes go to the deploy directory returned by `Filesystem.getDeployDirectory()` (often under `build/...`), not necessarily `src/main/deploy`. Check the `[Config] Saved config cache:` log line for the absolute path.

## Documentation

**[Config System Developer Guide](docs/config-system-guide.md)** — library overview and file-by-file reference.

## Quick start

```java
// Robot.robotInit()
configManager = new ConfigManager(
    new File(Filesystem.getDeployDirectory(), "robot-config.json"),
    new File(Filesystem.getDeployDirectory(), "config-cache.json"));

// Robot.robotPeriodic()
configManager.periodic();

// Subsystem code
PIDController armPid = configManager.getDocument().getPIDController("Arm/PIDController");
```

Dashboard controls: `Config/DebugMode`, `Config/Save`.
