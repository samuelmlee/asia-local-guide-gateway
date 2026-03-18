package com.asialocalguide.gateway.destination.domain;

import com.asialocalguide.gateway.core.domain.BookingProviderName;

import java.util.List;

/**
 * Input data for the destination ingestion pipeline.
 *
 * @param providerName    the booking provider that supplied the raw destinations
 * @param rawDestinations the list of provider destinations to be triaged and persisted
 */
public record DestinationIngestionInput(BookingProviderName providerName, List<CommonDestination> rawDestinations) {
}
