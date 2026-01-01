package com.asialocalguide.gateway.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asialocalguide.gateway.core.domain.destination.DestinationProviderMapping;
import com.asialocalguide.gateway.core.domain.destination.DestinationProviderMappingId;
import com.asialocalguide.gateway.core.repository.custom.CustomDestinationProviderMappingRepository;


public interface DestinationProviderMappingRepository
    extends JpaRepository<DestinationProviderMapping, DestinationProviderMappingId>, CustomDestinationProviderMappingRepository {}
