package com.asialocalguide.gateway.viator.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.Arrays;
import lombok.Data;

@Data
public class ViatorDestinationDTO {

  private Long destinationId;

  private String name;

  private String type;

  private Long[] lookupIds;

  @JsonSetter("lookupId")
  public void setLookupIds(String lookupIdString) {

    this.lookupIds =
        Arrays.stream(lookupIdString.split("\\.")).map(Long::parseLong).toArray(Long[]::new);
  }
}
