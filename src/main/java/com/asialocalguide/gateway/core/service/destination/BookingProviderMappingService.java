package com.asialocalguide.gateway.core.service.destination;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.repository.BookingProviderMappingRepository;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class BookingProviderMappingService {

  private final BookingProviderMappingRepository bookingProviderMappingRepository;

  public BookingProviderMappingService(BookingProviderMappingRepository bookingProviderMappingRepository) {
    this.bookingProviderMappingRepository = bookingProviderMappingRepository;
  }

  public Set<String> findProviderDestinationIdsByProviderName(BookingProviderName providerName) {
    return bookingProviderMappingRepository.findProviderDestinationIdsByProviderName(providerName);
  }
}
