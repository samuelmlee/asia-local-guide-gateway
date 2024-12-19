package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProviderMapping;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.Destination;
import com.asialocalguide.gateway.core.dto.DestinationDTO;
import com.asialocalguide.gateway.core.exception.DestinationRepositoryException;
import com.asialocalguide.gateway.core.repository.BookingProviderMappingRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.viator.service.ViatorDestinationService;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
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

    Set<String> destinationIds =
        bookingProviderMappingRepository.findProviderDestinationIdsByProviderName(
            BookingProviderName.VIATOR);

    List<Destination> destinationsToSave =
        destinations.stream()
            .filter(
                d -> {
                  // Only one new mapping for Destination fetched from Viator API
                  BookingProviderMapping newMapping = d.getBookingProviderMappings().getFirst();

                  if (newMapping == null) {
                    log.warn("No BookingProviderMapping to persist for Viator Destination: {}", d);

                    return false;
                  }

                  return !destinationIds.contains(newMapping.getProviderDestinationId());
                })
            .toList();

    destinationRepository.saveAll(destinationsToSave);
  }

  public List<DestinationDTO> getAllDestinations() {
    List<Destination> destinations = destinationRepository.findAll();

    if (destinations.isEmpty()) {
      throw new DestinationRepositoryException("No destinations returned from the Repository");
    }

    Locale locale = LocaleContextHolder.getLocale();

    String userLocaleCode = locale.getLanguage();

    return List.of();

    //    return destinations.stream()
    //        .map(d -> DestinationDTO.of(d.getId(), d.ge, d.getType()))
    //        .collect(Collectors.toList());
  }
}
