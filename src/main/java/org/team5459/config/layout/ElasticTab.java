package org.team5459.config.layout;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

/** One Elastic dashboard tab. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ElasticTab {

  public String name;
  public GridLayout grid_layout;

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class GridLayout {
    /** Nested group widgets such as {@code List Layout}. */
    public List<ElasticContainer> layouts = new ArrayList<>();

    /** Top-level widgets on the tab. */
    public List<ElasticContainer> containers = new ArrayList<>();
  }
}
