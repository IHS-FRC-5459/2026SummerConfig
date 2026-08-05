# Config

FRC robot project with a typed JSON configuration library (`org.team5459.config`).

Robot settings live in `src/main/deploy/robot-config.json`. `ConfigManager` publishes them to NetworkTables under `/Config`.

- **Debug** (`!FMS` and `Config/DebugMode`): getters read live NT-backed values; edits autosave to `config-cache.json`; promote to `robot-config.json` with `Config/Save` or when `elastic-layout.json` content changes (Save As).
- **Match** (FMS, or DebugMode off): getters read JSON defaults; NT is display-only; no JSON writes. Ship updated defaults via normal deploy.

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
