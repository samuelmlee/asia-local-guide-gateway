package com.asialocalguide.gateway.viator.dto;

import com.asialocalguide.gateway.core.domain.destination.Coordinates;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ViatorDestinationDTO(
    @NotNull Long destinationId,
    @NotNull String name,
    @NotNull String type,
    @NotNull
        @JsonDeserialize(using = LookupIdDeserializer.class)
        @JsonSerialize(using = LookupIdSerializer.class)
        @JsonProperty("lookupId")
        @NotNull
        List<Long> lookupIds,
    @NotNull Coordinates center) {}
