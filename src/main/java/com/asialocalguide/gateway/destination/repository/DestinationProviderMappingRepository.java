package com.asialocalguide.gateway.destination.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asialocalguide.gateway.destination.domain.DestinationProviderMapping;
import com.asialocalguide.gateway.destination.domain.DestinationProviderMappingId;
import com.asialocalguide.gateway.destination.repository.custom.CustomDestinationProviderMappingRepository;

public interface DestinationProviderMappingRepository
		extends JpaRepository<DestinationProviderMapping, DestinationProviderMappingId>,
		CustomDestinationProviderMappingRepository {
}

