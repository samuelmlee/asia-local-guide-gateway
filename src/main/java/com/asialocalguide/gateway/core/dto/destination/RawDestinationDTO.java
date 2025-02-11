package com.asialocalguide.gateway.core.dto.destination;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.Coordinates;
import com.asialocalguide.gateway.core.domain.destination.DestinationType;
import java.util.List;

public record RawDestinationDTO(
    String destinationId,
    List<Translation> names,
    DestinationType type,
    Coordinates centerCoordinates,
    BookingProviderName providerType,
    String countryIsoCode) {

  public record Translation(String languageCode, String name) {}
}
