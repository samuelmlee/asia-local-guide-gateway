package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProviderMapping;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.Destination;
import com.asialocalguide.gateway.core.repository.BookingProviderMappingRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.viator.service.ViatorDestinationService;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DestinationService {

  private final ViatorDestinationService viatorDestinationService;

  private final DestinationRepository destinationRepository;

  private final BookingProviderMappingRepository bookingProviderMappingRepository;

  private static final String defaultLocale = "en";

  public DestinationService(
      ViatorDestinationService viatorDestinationService,
      DestinationRepository destinationRepository,
      BookingProviderMappingRepository bookingProviderMappingRepository) {

    this.viatorDestinationService = viatorDestinationService;
    this.destinationRepository = destinationRepository;
    this.bookingProviderMappingRepository = bookingProviderMappingRepository;
  }

  public void syncViatorDestinations() {

    List<Destination> destinations = viatorDestinationService.getAllDestinations();

    Set<String> viatorDestinationIds =
        bookingProviderMappingRepository.findProviderDestinationIdsByProviderName(
            BookingProviderName.VIATOR);

    List<Destination> destinationsToSave =
        destinations.stream()
            .filter(
                d -> {
                  BookingProviderMapping newMapping =
                      d.getBookingProviderMappings().stream()
                          .filter(m -> m.getProviderName().equals(BookingProviderName.VIATOR))
                          .findFirst()
                          .orElse(null);

                  if (newMapping == null) {
                    log.warn("No BookingProviderMapping to persist for Viator Destination: {}", d);

                    return false;
                  }

                  return !viatorDestinationIds.contains(newMapping.getProviderDestinationId());
                })
            .toList();

    destinationRepository.saveAll(destinationsToSave);
  }
}
