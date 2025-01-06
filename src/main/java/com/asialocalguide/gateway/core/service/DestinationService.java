package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.Destination;
import com.asialocalguide.gateway.core.domain.DestinationProviderMapping;
import com.asialocalguide.gateway.core.domain.DestinationTranslation;
import com.asialocalguide.gateway.core.dto.DestinationDTO;
import com.asialocalguide.gateway.core.repository.BookingProviderMappingRepository;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
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

  private final BookingProviderRepository bookingProviderRepository;

  private final BookingProviderMappingRepository bookingProviderMappingRepository;

  private static final String DEFAULT_LANGUAGE_CODE = "en";

  public DestinationService(
      ViatorDestinationService viatorDestinationService,
      DestinationRepository destinationRepository,
      BookingProviderRepository bookingProviderRepository,
      BookingProviderMappingRepository bookingProviderMappingRepository) {

    this.viatorDestinationService = viatorDestinationService;
    this.destinationRepository = destinationRepository;
    this.bookingProviderRepository = bookingProviderRepository;
    this.bookingProviderMappingRepository = bookingProviderMappingRepository;
  }

  public void syncViatorDestinations() {

    List<Destination> destinations = viatorDestinationService.getAllDestinations();

    BookingProvider viatorProvider =
        bookingProviderRepository
            .findByName("VIATOR")
            .orElseThrow(() -> new IllegalStateException("Viator BookingProvider not found"));

    Set<String> viatorDestinationIds =
        bookingProviderMappingRepository.findProviderDestinationIdsByProviderId(
            viatorProvider.getId());

    List<Destination> destinationsToSave =
        destinations.stream()
            .filter(
                d -> isNewBookingProviderMapping(d, viatorDestinationIds, viatorProvider.getId()))
            .toList();

    destinationRepository.saveAll(destinationsToSave);
  }

  private static boolean isNewBookingProviderMapping(
      Destination d, Set<String> viatorDestinationIds, Long viatorProviderId) {
    DestinationProviderMapping newMapping = d.getBookingProviderMapping(viatorProviderId);

    if (newMapping == null) {
      log.warn("No BookingProviderMapping to persist for Viator Destination: {}", d);

      return false;
    }

    return !viatorDestinationIds.contains(newMapping.getProviderDestinationId());
  }

  public List<DestinationDTO> getAutocompleteSuggestions(String query) {

    Locale locale = LocaleContextHolder.getLocale();

    List<Destination> destinations =
        destinationRepository.findCityByTranslationsForLocaleAndDestinationName(
            locale.getLanguage(), query);

    return destinations.stream()
        .map(
            destination -> {
              String translationName = resolveTranslationName(destination, locale);

              String parentName =
                  destination.getParentDestination() != null
                      ? resolveTranslationName(destination.getParentDestination(), locale)
                      : "";

              return DestinationDTO.of(
                  destination.getId(), translationName, destination.getType(), parentName);
            })
        .toList();
  }

  private String resolveTranslationName(Destination destination, Locale locale) {
    return destination.getDestinationTranslations().stream()
        .filter(t -> t.getLocale().equals(locale.getLanguage()))
        .findFirst()
        .or(
            () ->
                destination.getDestinationTranslations().stream()
                    .filter(t -> t.getLocale().equals(DEFAULT_LANGUAGE_CODE))
                    .findFirst())
        .map(DestinationTranslation::getDestinationName)
        .orElse("");
  }
}
