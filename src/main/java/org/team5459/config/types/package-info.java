/**
 * Concrete {@link org.team5459.config.ConfigNode} implementations for every supported JSON {@code
 * type} string.
 *
 * <h2>Patterns</h2>
 *
 * <p>Most files here follow one of three shapes:
 *
 * <ul>
 *   <li><b>Scalar/array leaves</b> — e.g. {@link DoubleNode}, {@link IntNode}, {@link StringNode}.
 *       These store a primitive payload, expose {@code getValue}/{@code setValue}, and sync
 *       directly to NetworkTables entries.
 *   <li><b>Immutable composites</b> — extend {@link org.team5459.config.ValueConfigNode} and
 *       implement {@code buildValue()} to construct a WPILib object from typed child fields. The
 *       cached value is rebuilt whenever child fields change (including after remote NT edits).
 *   <li><b>Live composites</b> — extend {@link org.team5459.config.CompositeConfigNode} directly
 *       and own mutable runtime objects such as {@link PIDControllerNode} or {@link
 *       ProfiledPIDControllerNode}. Callers receive the same live instance across reads.
 * </ul>
 *
 * <p>Several geometry and unit types accept alternative field layouts (for example {@code deg} vs
 * {@code rad}, nested {@code translation} vs inline {@code x}/{@code y}). See the class Javadoc on
 * those nodes for supported shapes.
 *
 * <p>Every class in this package must be registered in {@link
 * org.team5459.config.ConfigTypeRegistry} so Jackson can deserialize the matching {@code type}
 * discriminator.
 */
package org.team5459.config.types;
