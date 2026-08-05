/**
 * Typed JSON configuration library for FRC robots.
 *
 * <h2>Overview</h2>
 *
 * <p>Configuration files are JSON objects whose entries use an explicit {@code type}/{@code value}
 * schema. Each entry's {@code type} field selects a concrete {@link org.team5459.config.ConfigNode}
 * implementation registered in {@link org.team5459.config.ConfigTypeRegistry}. Folders group
 * related values using slash-separated paths such as {@code Arm/PIDController/p}.
 *
 * <h2>Typical robot workflow</h2>
 *
 * <ol>
 *   <li>{@link org.team5459.config.ConfigManager} loads {@code robot-config.json} defaults and
 *       publishes them under {@code /Config}.
 *   <li><b>Debug</b> ({@code !FMS} and {@code Config/DebugMode}): NT edits update the live
 *       document; getters read that NT-backed document; edits autosave to {@code
 *       config-cache.json}. Promote via {@code Config/Save} or when {@code elastic-layout.json}
 *       content changes. New scalars under {@code /Config} auto-register (Elastic Custom + rebind).
 *   <li><b>Match</b> (FMS, or DebugMode off): NT is write-only for display; getters read JSON
 *       defaults; no JSON writes.
 *   <li>Create constants via JSON, {@code /Config/templates/template*} widgets, or Custom+rebind.
 *   <li>Subsystems read through {@link org.team5459.config.ConfigDocument} typed getters.
 * </ol>
 *
 * <h2>Node hierarchy</h2>
 *
 * <ul>
 *   <li>{@link org.team5459.config.ConfigNode} - base type for every JSON entry.
 *   <li>{@link org.team5459.config.types.FolderNode} - organizational container with named
 *       children.
 *   <li>{@link org.team5459.config.CompositeConfigNode} - entry backed by named child fields;
 *       rebuilds runtime state when fields change.
 *   <li>{@link org.team5459.config.ValueConfigNode} - composite that materializes an immutable
 *       WPILib value on each sync.
 *   <li>Scalar nodes in {@link org.team5459.config.types} - leaf values editable over
 *       NetworkTables.
 * </ul>
 *
 * <h2>Error handling</h2>
 *
 * <p>Missing paths, wrong types, and missing composite fields log warnings through {@link
 * org.team5459.config.ConfigWarnings} and return safe defaults. Robot startup is never blocked by
 * bad config data.
 */
package org.team5459.config;
