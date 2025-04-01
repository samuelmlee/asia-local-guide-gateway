package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.BookingProviderName;

import java.util.Set;

public interface CustomBookingProviderMappingRepository {

    Set<String> findProviderDestinationIdsByProviderName(BookingProviderName providerName);
}
