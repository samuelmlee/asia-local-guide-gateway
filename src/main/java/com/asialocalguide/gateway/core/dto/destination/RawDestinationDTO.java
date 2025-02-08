package com.asialocalguide.gateway.core.dto.destination;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.Coordinates;
import com.asialocalguide.gateway.core.domain.destination.DestinationType;

public record RawDestinationDTO(
    String destinationId,
    String name,
    DestinationType type,
    Coordinates coordinates,
    BookingProviderName providerType,
    String localeCode) {}
