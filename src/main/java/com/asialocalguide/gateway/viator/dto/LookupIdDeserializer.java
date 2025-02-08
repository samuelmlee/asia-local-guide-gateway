package com.asialocalguide.gateway.viator.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LookupIdDeserializer extends JsonDeserializer<List<Long>> {

  @Override
  public List<Long> deserialize(
      JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    String lookupIdString = jsonParser.getText();
    List<Long> allIds =
        Arrays.stream(lookupIdString.split("\\."))
            .map(Long::parseLong)
            .collect(Collectors.toList());
    allIds.removeLast();

    return allIds;
  }
}
