package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.dto.destination.DestinationDTO;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.repository.BookingProviderMappingRepository;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.core.service.composer.DestinationProvider;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DestinationService {

  private final List<DestinationProvider> destinationProviders;

  private final DestinationSortingService destinationSortingService;

  private final DestinationRepository destinationRepository;

  private final BookingProviderRepository bookingProviderRepository;

  private final BookingProviderMappingRepository bookingProviderMappingRepository;

  public DestinationService(
      List<DestinationProvider> destinationProviders,
      DestinationSortingService destinationSortingService,
      DestinationRepository destinationRepository,
      BookingProviderRepository bookingProviderRepository,
      BookingProviderMappingRepository bookingProviderMappingRepository) {

    this.destinationProviders = destinationProviders;
    this.destinationSortingService = destinationSortingService;
    this.destinationRepository = destinationRepository;
    this.bookingProviderRepository = bookingProviderRepository;
    this.bookingProviderMappingRepository = bookingProviderMappingRepository;
  }

  public void syncDestinations() {

    Map<BookingProviderName, Map<String, RawDestinationDTO>> providerToIsoToRawDestinationDTOs =
        destinationProviders.stream()
            .collect(
                Collectors.toMap(
                    DestinationProvider::getProviderName,
                    provider -> {
                      try {
                        List<RawDestinationDTO> destinations = provider.getDestinations();

                        List<RawDestinationDTO> filtered =
                            filterExistingDestinationByProvider(
                                destinations, provider.getProviderName());

                        return filtered.stream()
                            .collect(Collectors.toMap(RawDestinationDTO::countryIsoCode, d -> d));

                      } catch (Exception e) {
                        log.error(
                            "Failed to fetch destinations from provider: {}",
                            provider.getProviderName(),
                            e);
                        return Map.of();
                      }
                    }));

    destinationSortingService.triageRawDestinations(providerToIsoToRawDestinationDTOs);
  }

  public List<RawDestinationDTO> filterExistingDestinationByProvider(
      List<RawDestinationDTO> destinationDTOs, BookingProviderName providerName) {

    BookingProvider viatorProvider =
        bookingProviderRepository
            .findByName(providerName)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        String.format("BookingProvider not found for names: %s", providerName)));

    Set<String> existingProviderDestinationIds =
        bookingProviderMappingRepository.findProviderDestinationIdsByProviderId(
            viatorProvider.getId());

    return destinationDTOs.stream()
        .filter(d -> !existingProviderDestinationIds.contains(d.destinationId()))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<DestinationDTO> getAutocompleteSuggestions(String query) {

    Locale locale = LocaleContextHolder.getLocale();

    LanguageCode languageCode;
    try {
      languageCode = LanguageCode.from(locale.getLanguage());
    } catch (Exception e) {
      languageCode = LanguageCode.EN;
    }
    final LanguageCode finalLanguageCode = languageCode;

    List<Destination> destinations =
        destinationRepository.findCityOrRegionByTranslationsForLanguageCodeAndName(
            finalLanguageCode, query);

    return destinations.stream()
        .map(
            destination -> {
              String translationName = destination.getTranslation(finalLanguageCode).orElse("");

              if (translationName.isEmpty()) {
                return null;
              }

              String countryName =
                  destination.getCountry() != null
                      ? destination.getCountry().getTranslation(finalLanguageCode).orElse("")
                      : "";

              return DestinationDTO.of(
                  destination.getId(), translationName, destination.getType(), countryName);
            })
        .filter(Objects::nonNull)
        .toList();
  }
}
