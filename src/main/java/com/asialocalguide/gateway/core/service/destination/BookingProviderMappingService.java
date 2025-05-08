package com.asialocalguide.gateway.core.service.destination;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.repository.DestinationProviderMappingRepository;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class BookingProviderMappingService {

  private final DestinationProviderMappingRepository destinationProviderMappingRepository;

  public BookingProviderMappingService(DestinationProviderMappingRepository destinationProviderMappingRepository) {
    this.destinationProviderMappingRepository = destinationProviderMappingRepository;
  }

  public Set<String> findProviderDestinationIdsByProviderName(BookingProviderName providerName) {
    return destinationProviderMappingRepository.findProviderDestinationIdsByProviderName(providerName);
  }
}
