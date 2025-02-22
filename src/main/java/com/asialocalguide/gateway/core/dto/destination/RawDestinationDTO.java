package com.asialocalguide.gateway.core.dto.destination;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.Coordinates;
import com.asialocalguide.gateway.core.domain.destination.DestinationType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RawDestinationDTO(
    @NotNull String destinationId,
    @NotNull @NotEmpty List<Translation> names,
    @NotNull DestinationType type,
    @NotNull Coordinates centerCoordinates,
    @NotNull BookingProviderName providerName,
    @NotNull String countryIsoCode) {

  public record Translation(@NotNull String languageCode, @NotNull String name) {}
}
