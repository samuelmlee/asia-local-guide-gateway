package com.asialocalguide.gateway.viator.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Jackson deserializer that converts the Viator {@code lookupId} dot-separated string
 * (e.g. {@code "732.4426.28826"}) into a list of {@code Long} IDs, discarding the last element
 * (which represents the destination itself).
 */
public class LookupIdDeserializer extends JsonDeserializer<List<Long>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Long> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, NumberFormatException {
		String lookupIdString = jsonParser.getText();
		List<Long> allIds = Arrays.stream(lookupIdString.split("\\."))
				.map(Long::parseLong)
				.collect(Collectors.toList());
		allIds.removeLast();

		return allIds;
	}
}
