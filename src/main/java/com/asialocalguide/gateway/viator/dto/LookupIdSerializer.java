package com.asialocalguide.gateway.viator.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Jackson serializer that converts a list of {@code Long} IDs back to the Viator
 * {@code lookupId} dot-separated string format (e.g. {@code "732.4426"}).
 */
public class LookupIdSerializer extends JsonSerializer<List<Long>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(List<Long> lookupIds, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			throws IOException {

		String lookupIdString = lookupIds.stream().map(String::valueOf).collect(Collectors.joining("."));
		jsonGenerator.writeString(lookupIdString);
	}
}
