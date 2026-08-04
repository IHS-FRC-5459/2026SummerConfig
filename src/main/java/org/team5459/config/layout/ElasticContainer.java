package org.team5459.config.layout;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * One Elastic dashboard widget.
 *
 * <p>List layouts may nest other widgets in {@link #children}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ElasticContainer {

  public String title;
  public Double x;
  public Double y;
  public Double width;
  public Double height;
  public String type;
  public ElasticProperties properties;
  public List<ElasticContainer> children = new ArrayList<>();
}
