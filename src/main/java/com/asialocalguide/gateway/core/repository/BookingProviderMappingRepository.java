package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.destination.DestinationProviderMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingProviderMappingRepository
        extends JpaRepository<DestinationProviderMapping, Long>, CustomBookingProviderMappingRepository {
}
