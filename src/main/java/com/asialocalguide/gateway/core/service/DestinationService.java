package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.dto.destination.DestinationDTO;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.repository.BookingProviderMappingRepository;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.core.service.composer.DestinationProvider;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DestinationService {

  private final List<DestinationProvider> destinationProviders;

  private final DestinationPersistenceService destinationPersistenceService;

  private final DestinationRepository destinationRepository;

  private final BookingProviderRepository bookingProviderRepository;

  private final BookingProviderMappingRepository bookingProviderMappingRepository;

  private static final String DEFAULT_LANGUAGE_CODE = "en";

  public DestinationService(
      List<DestinationProvider> destinationProviders,
      DestinationPersistenceService destinationPersistenceService,
      DestinationRepository destinationRepository,
      BookingProviderRepository bookingProviderRepository,
      BookingProviderMappingRepository bookingProviderMappingRepository) {

    this.destinationProviders = destinationProviders;
    this.destinationPersistenceService = destinationPersistenceService;
    this.destinationRepository = destinationRepository;
    this.bookingProviderRepository = bookingProviderRepository;
    this.bookingProviderMappingRepository = bookingProviderMappingRepository;
  }

  public void syncDestinations() {
    //    List<RawDestinationDTO> rawDestinationDTOS =
    //        destinationProviders.stream()
    //            .flatMap(this::syncDestinationsFromProvider)
    //            .collect(Collectors.toList());
  }

  public List<RawDestinationDTO> syncDestinationsFromProvider(
      DestinationProvider destinationProvider) {
    return List.of();
    //    BookingProviderName providerName = destinationProvider.getProviderType();
    //    SupportedLocale defaultLocale = SupportedLocale.getDefaultLocale();
    //
    //    List<RawDestinationDTO> rawDestinations;
    //    try {
    //
    //      rawDestinations = destinationProvider.getDestinations(defaultLocale);
    //    } catch (Exception e) {
    //      log.error("Failed to fetch destinations from provider: {}", providerName, e);
    //      return List.of();
    //    }
    //
    //    BookingProvider viatorProvider =
    //        bookingProviderRepository
    //            .findByName("VIATOR")
    //            .orElseThrow(() -> new IllegalStateException("Viator BookingProvider not found"));
    //
    //    Set<String> viatorDestinationIds =
    //        bookingProviderMappingRepository.findProviderDestinationIdsByProviderId(
    //            viatorProvider.getId());
    //
    //    List<ViatorDestinationDTO> newDestinationDTOs =
    //        rawDestinations.stream()
    //            .filter(d -> isNewViatorDestinationDto(d, viatorDestinationIds))
    //            .collect(Collectors.toCollection(ArrayList::new));
    //
    //    destinationPersistenceService.buildAndSaveDestinationsFromViatorDtos(newDestinationDTOs);
  }

  private static boolean isNewViatorDestinationDto(
      ViatorDestinationDTO d, Set<String> viatorDestinationIds) {
    return false;
    //
    //    // Update method when Destination can be added by other providers to check for name and
    //    // coordinates
    //
    //    return !viatorDestinationIds.contains(d.getDestinationId().toString());
  }

  public List<DestinationDTO> getAutocompleteSuggestions(String query) {

    Locale locale = LocaleContextHolder.getLocale();

    LanguageCode languageCode = LanguageCode.fromLocale(locale).orElse(LanguageCode.EN);

    List<Destination> destinations =
        destinationRepository.findCityOrRegionByTranslationsForLanguageCodeAndDestinationName(
            locale.getLanguage(), query);

    return destinations.stream()
        .map(
            destination -> {
              String translationName = destination.getTranslation(languageCode).orElse("");

              if (translationName.isEmpty()) {
                return null;
              }

              // Countries fo not have an additional country name
              String countryName =
                  destination.getCountry() != null
                      ? destination.getCountry().getTranslation(languageCode).orElse("")
                      : "";

              return DestinationDTO.of(
                  destination.getId(), translationName, destination.getType(), countryName);
            })
        .filter(Objects::nonNull)
        .toList();
  }
}
