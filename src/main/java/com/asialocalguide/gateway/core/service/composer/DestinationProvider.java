package com.asialocalguide.gateway.core.service.composer;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.destination.domain.CommonDestination;

import java.util.List;

/**
 * Strategy interface for booking provider integrations that supply destination data
 * for ingestion into the local database.
 */
public interface DestinationProvider {

	/**
	 * Returns the {@link BookingProviderName} this implementation targets.
	 *
	 * @return the provider name; never {@code null}
	 */
	BookingProviderName getProviderName();

	/**
	 * Fetches the full list of destinations available from this provider.
	 *
	 * @return list of common destination objects; never {@code null}
	 */
	List<CommonDestination> getDestinations();
}
