package com.asialocalguide.gateway.destination.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asialocalguide.gateway.destination.domain.DestinationProviderMapping;
import com.asialocalguide.gateway.destination.domain.DestinationProviderMappingId;
import com.asialocalguide.gateway.destination.repository.custom.CustomDestinationProviderMappingRepository;

/**
 * Repository for {@link DestinationProviderMapping} entities.
 *
 * <p>Extends {@link JpaRepository} for standard CRUD operations and
 * {@link CustomDestinationProviderMappingRepository} for provider-specific ID queries.
 */
public interface DestinationProviderMappingRepository
		extends JpaRepository<DestinationProviderMapping, DestinationProviderMappingId>,
		CustomDestinationProviderMappingRepository {
}

