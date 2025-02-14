package com.asialocalguide.gateway.viator.dto;

import com.asialocalguide.gateway.core.domain.destination.Coordinates;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ViatorDestinationDTO(
        @NotNull Long destinationId,
        @NotNull String name,
        @NotNull String type,
        @NotNull @JsonDeserialize(using = LookupIdDeserializer.class) @JsonProperty("lookupId")
        @NotNull List<Long> lookupIds,
        @NotNull String localeCode,
        @NotNull Coordinates center) {
}
