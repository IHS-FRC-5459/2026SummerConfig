package org.team5459.config;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonTree {

  private final JsonNode root;

  public JsonTree(JsonNode root) {
    this.root = root;
  }

  public JsonNode getRoot() {
    return root;
  }
}
