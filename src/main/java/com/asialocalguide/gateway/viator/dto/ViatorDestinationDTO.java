package com.asialocalguide.gateway.viator.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class ViatorDestinationDTO {

  private Long destinationId;

  private String name;

  private String type;

  private List<Long> lookupIds;

  @JsonSetter("lookupId")
  public void setLookupIds(String lookupIdString) {

    List<Long> allIds =
        Arrays.stream(lookupIdString.split("\\."))
            .map(Long::parseLong)
            .collect(Collectors.toList());
    // Last lookupId is own destinationId
    allIds.removeLast();

    this.lookupIds = allIds;
  }
}
