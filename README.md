# Config

FRC robot project with a typed JSON configuration library (`org.team5459.config`).

Robot settings (PID gains, poses, feedforwards, etc.) live in `src/main/deploy/robot-config.json`. At runtime, `ConfigManager` loads the file, publishes values to NetworkTables for live tuning, autosaves edits to `config-cache.json`, and commits changes to the real config when Save is pressed.

## Documentation

**[Config System Developer Guide](docs/config-system-guide.md)** — overview of the library, file-by-file breakdown, composite node patterns, error/warning handling, and quick reference.

## Quick start

```java
// Robot.robotInit()
configManager = new ConfigManager(
    new File(Filesystem.getDeployDirectory(), "robot-config.json"),
    new File(Filesystem.getDeployDirectory(), "config-cache.json"));

// Subsystem code
PIDController armPid = configManager.getDocument().getPIDController("Arm/PIDController");
```

Config values are published under `/Config` on NetworkTables. The Save button defaults to `ConfigManager/Save`.
