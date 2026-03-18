package com.asialocalguide.gateway.destination.repository.custom;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import java.util.Set;

/**
 * Custom repository interface for {@link com.asialocalguide.gateway.destination.domain.DestinationProviderMapping}
 * queries that filter by booking provider.
 */
public interface CustomDestinationProviderMappingRepository {

	/**
	 * Returns the set of provider-assigned destination IDs that are already mapped for the given provider.
	 *
	 * @param providerName the booking provider to filter by
	 * @return set of provider destination IDs; never {@code null}
	 */
	Set<String> findProviderDestinationIdsByProviderName(BookingProviderName providerName);
}
