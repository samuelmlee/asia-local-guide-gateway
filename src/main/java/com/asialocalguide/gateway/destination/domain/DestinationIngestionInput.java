package com.asialocalguide.gateway.destination.domain;

import com.asialocalguide.gateway.core.domain.BookingProviderName;

import java.util.List;

public record DestinationIngestionInput(BookingProviderName providerName, List<CommonDestination> rawDestinations) {
}
