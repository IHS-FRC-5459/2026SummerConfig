package org.team5459.config;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Lightweight wrapper around Jackson's {@link JsonNode}.
 *
 * <p>This abstraction prevents the rest of the library from depending directly on Jackson APIs and
 * leaves room for future metadata or alternate implementations.
 */
public class JsonTree {

  /** Root node of the parsed JSON document. */
  private final JsonNode root;

  /**
   * Creates a new JSON tree wrapper.
   *
   * @param root Root JSON node
   */
  public JsonTree(JsonNode root) {
    this.root = root;
  }

  /**
   * Returns the root JSON node.
   *
   * @return Root node
   */
  public JsonNode getRoot() {
    return root;
  }
}
