package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.destination.DestinationProviderMapping;
import com.asialocalguide.gateway.core.repository.custom.CustomDestinationProviderMappingRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinationProviderMappingRepository
    extends JpaRepository<DestinationProviderMapping, Long>, CustomDestinationProviderMappingRepository {}
