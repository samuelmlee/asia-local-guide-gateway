package com.asialocalguide.gateway.destination.service;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.destination.repository.DestinationProviderMappingRepository;

import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Service for querying {@link com.asialocalguide.gateway.destination.domain.DestinationProviderMapping} data.
 */
@Service
public class DestinationProviderMappingService {

	private final DestinationProviderMappingRepository destinationProviderMappingRepository;

	/**
	 * @param destinationProviderMappingRepository repository for provider mapping queries
	 */
	public DestinationProviderMappingService(
			DestinationProviderMappingRepository destinationProviderMappingRepository) {
		this.destinationProviderMappingRepository = destinationProviderMappingRepository;
	}

	/**
	 * Returns the set of provider-assigned destination IDs already mapped for the given provider.
	 *
	 * <p>Used during ingestion to skip destinations that have already been imported.
	 *
	 * @param providerName the booking provider to query
	 * @return set of provider destination IDs; never {@code null}
	 */
	public Set<String> findProviderDestinationIdsByProviderName(BookingProviderName providerName) {
		return destinationProviderMappingRepository.findProviderDestinationIdsByProviderName(providerName);
	}
}
