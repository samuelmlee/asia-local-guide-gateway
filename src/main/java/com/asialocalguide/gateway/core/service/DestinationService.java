package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderType;
import com.asialocalguide.gateway.core.domain.Destination;
import com.asialocalguide.gateway.core.dto.DestinationDTO;
import com.asialocalguide.gateway.core.exception.DestinationRepositoryException;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.viator.service.ViatorDestinationService;
import java.util.List;
import java.util.Locale;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class DestinationService {

  private final ViatorDestinationService viatorDestinationService;

  private final DestinationRepository destinationRepository;

  private final BookingProviderRepository bookingProviderRepository;

  public DestinationService(
      ViatorDestinationService viatorDestinationService,
      DestinationRepository destinationRepository,
      BookingProviderRepository bookingProviderRepository) {

    this.viatorDestinationService = viatorDestinationService;
    this.destinationRepository = destinationRepository;
    this.bookingProviderRepository = bookingProviderRepository;
  }

  public void syncViatorDestinations() {

    List<Destination> destinations = viatorDestinationService.getAllDestinations();

    BookingProvider provider = bookingProviderRepository.findByName(BookingProviderType.VIATOR);

    //    destinations.stream()
    //        .filter(
    //            d -> {
    //              return
    // !destinationRepository.findByProviderAndProviderDestinationId(d.getName());
    //            })
    //        .forEach(destinationRepository::save);
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
