package org.team5459.config.layout;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Elastic widget {@code properties} object.
 *
 * <p>{@code value} is untyped JSON so scalars and PID objects can share the same field.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ElasticProperties {

  public String topic;
  public Double period;
  public String data_type;
  public JsonNode value;
  public Boolean show_submit_button;
}
