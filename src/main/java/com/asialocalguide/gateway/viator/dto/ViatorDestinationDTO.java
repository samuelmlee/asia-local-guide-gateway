package com.asialocalguide.gateway.viator.dto;

import com.asialocalguide.gateway.destination.domain.Coordinates;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Viator API representation of a single destination.
 *
 * <p>The {@code lookupId} field is serialized as a dot-separated string (e.g. {@code "732.4426.28826"})
 * and deserialized to a list of parent and self IDs, with the last element removed.
 *
 * @param destinationId Viator's numeric destination identifier
 * @param name          localized destination name
 * @param type          Viator destination type (e.g. {@code "CITY"}, {@code "COUNTRY"})
 * @param lookupIds     hierarchy of parent destination IDs derived from the {@code lookupId} field
 * @param center        geographic centre coordinates of the destination
 */
public record ViatorDestinationDTO(@NotNull Long destinationId, @NotNull String name, @NotNull String type,
		@NotNull @JsonDeserialize(using = LookupIdDeserializer.class) @JsonSerialize(using = LookupIdSerializer.class) @JsonProperty("lookupId") @NotNull List<Long> lookupIds,
		@NotNull Coordinates center) {
}
