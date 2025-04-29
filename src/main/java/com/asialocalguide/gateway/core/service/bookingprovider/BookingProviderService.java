package com.asialocalguide.gateway.core.service.bookingprovider;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BookingProviderService {

  private final BookingProviderRepository bookingProviderRepository;

  public BookingProviderService(BookingProviderRepository bookingProviderRepository) {
    this.bookingProviderRepository = bookingProviderRepository;
  }

  public Optional<BookingProvider> getBookingProviderByName(BookingProviderName providerName) {
    return bookingProviderRepository.findByName(providerName);
  }
}
