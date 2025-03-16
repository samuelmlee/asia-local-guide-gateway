package com.asialocalguide.gateway.core.domain.destination;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CommonDestination(
        @NotNull String destinationId,
        @NotNull @NotEmpty List<Translation> names,
        @NotNull DestinationType type,
        Coordinates centerCoordinates,
        @NotNull BookingProviderName providerName,
        @NotNull String countryIsoCode) {

    public record Translation(@NotNull String languageCode, @NotNull String name) {
    }
}
