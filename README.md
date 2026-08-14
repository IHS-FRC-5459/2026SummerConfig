For sim connecting to elastic, use 127.0.27.1

# Config

FRC robot project with a typed JSON configuration library (`org.team5459.config`).

Robot settings live in `src/main/deploy/robot-config.json`. `ConfigManager` publishes them to NetworkTables under `/Config`.

- **Debug** (`!FMS` and `Config/DebugMode`): getters read live NT-backed values; edits autosave to `config-cache.json`; promote to `robot-config.json` with `Config/Save` or when `elastic-layout.json` content changes (Save As).
- **Match** (FMS, or DebugMode off): getters read JSON defaults; NT is display-only; no JSON writes. Ship updated defaults via normal deploy.

## Creating constants (all supported)

1. **JSON / code** — add entries to `robot-config.json` (typed schema) and deploy.
2. **Create panel** (debug) — on the Create tab:
   - Type chooser → `/Config/Create/Type` (Folder plus scalars/composites)
   - Folder chooser → `/Config/Create/Folder` (`(root)` or any folder path, including nested like `Claw/Intake`)
   - Name text → `/Config/Create/Name`
   - Go Toggle Switch → `/Config/Create/Go` (flips on to create; robot clears it)
   Flip Go to insert a zero/empty default of that type (or an empty folder).
3. **Delete panel** (debug) — on the Delete tab:
   - Path chooser → `/Config/Delete/Path` (any constant or folder; `(none)` = no-op)
   - Go Toggle Switch → `/Config/Delete/Go`
   Flip Go to remove the path from the live document / `config-cache.json`. Deleting a folder removes all children. Press **Save** to promote into `robot-config.json`.
4. **Elastic Custom** — create a scalar, rebind topic to `/Config/...` (debug auto-register).

Elastic does **not** auto-reload `elastic-layout.json` — re-import/open that layout after Create/Delete UI changes.

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
