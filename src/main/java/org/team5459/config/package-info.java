/**
 * Typed JSON configuration library for FRC robots.
 *
 * <h2>Overview</h2>
 *
 * <p>Configuration files are JSON objects whose entries use an explicit schema:
 *
 * <pre>{@code
 * "Arm/PIDController": {
 *   "type": "PIDController",
 *   "value": {
 *     "p": { "type": "double", "value": 0.1 },
 *     "i": { "type": "double", "value": 0.01 },
 *     "d": { "type": "double", "value": 0.001 }
 *   }
 * }
 * }</pre>
 *
 * <p>Each entry's {@code type} field selects a concrete {@link org.team5459.config.ConfigNode}
 * implementation registered in {@link org.team5459.config.ConfigTypeRegistry}. Folders group
 * related values using slash-separated paths such as {@code Arm/PIDController/p}.
 *
 * <h2>Typical robot workflow</h2>
 *
 * <ol>
 *   <li>{@link org.team5459.config.TypedConfigLoader#load(java.io.File)} parses JSON into a
 *       {@link org.team5459.config.ConfigDocument}.
 *   <li>Subsystems read values through typed getters, e.g. {@code
 *       document.getPIDController("Arm/PIDController")}.
 *   <li>{@link org.team5459.config.TypedNetworkTableSync#publish(ConfigDocument)} mirrors the tree
 *       under the {@code /Config} NetworkTables table for Elastic or other dashboards.
 *   <li>{@link org.team5459.config.TypedNetworkTableSync#listen(ConfigDocument, Runnable)} applies
 *       remote scalar edits back into memory and optionally triggers a save.
 *   <li>{@link org.team5459.config.TypedConfigSaver#save(java.io.File, ConfigDocument)} writes
 *       schema-only JSON via {@link org.team5459.config.ConfigJsonWriter}, avoiding Jackson bean
 *       leakage from runtime getters.
 * </ol>
 *
 * <h2>Node hierarchy</h2>
 *
 * <ul>
 *   <li>{@link org.team5459.config.ConfigNode} — base type for every JSON entry.
 *   <li>{@link org.team5459.config.types.FolderNode} — organizational container with named
 *       children.
 *   <li>{@link org.team5459.config.CompositeConfigNode} — entry backed by named child fields;
 *       rebuilds runtime state when fields change.
 *   <li>{@link org.team5459.config.ValueConfigNode} — composite that materializes an immutable
 *       WPILib value on each sync.
 *   <li>Scalar nodes in {@link org.team5459.config.types} — leaf values editable over
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
