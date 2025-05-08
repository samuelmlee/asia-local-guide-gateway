package com.asialocalguide.gateway.core.repository.custom;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import java.util.Set;

public interface CustomDestinationProviderMappingRepository {

  Set<String> findProviderDestinationIdsByProviderName(BookingProviderName providerName);
}
