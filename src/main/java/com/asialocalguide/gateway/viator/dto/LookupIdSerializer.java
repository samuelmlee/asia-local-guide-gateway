package com.asialocalguide.gateway.viator.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class LookupIdSerializer extends JsonSerializer<List<Long>> {
  @Override
  public void serialize(List<Long> lookupIds, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {

    String lookupIdString = lookupIds.stream().map(String::valueOf).collect(Collectors.joining("."));
    jsonGenerator.writeString(lookupIdString);
  }
}
