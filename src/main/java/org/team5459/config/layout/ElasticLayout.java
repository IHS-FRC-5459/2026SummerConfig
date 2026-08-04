package org.team5459.config.layout;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * Root object for an Elastic Save As layout file (also used as {@code robot-config.json}).
 *
 * <p>{@code version} / {@code grid_size} are untyped because Elastic may write {@code 1} or {@code
 * 1.0}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ElasticLayout {

  public Object version;
  public Object grid_size;
  public List<ElasticTab> tabs = new ArrayList<>();
}
