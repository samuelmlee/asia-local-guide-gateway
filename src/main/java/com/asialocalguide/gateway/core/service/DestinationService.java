package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.dto.destination.DestinationDTO;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.core.service.composer.DestinationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DestinationService {

  private final List<DestinationProvider> destinationProviders;

  private final DestinationSortingService destinationSortingService;

  private final DestinationRepository destinationRepository;

  public DestinationService(
      List<DestinationProvider> destinationProviders,
      DestinationSortingService destinationSortingService,
      DestinationRepository destinationRepository) {
    this.destinationProviders = destinationProviders;
    this.destinationSortingService = destinationSortingService;
    this.destinationRepository = destinationRepository;
  }

  public void syncDestinations() {

    Map<BookingProviderName, List<RawDestinationDTO>> providerToIsoToRawDestinationDTOs =
        destinationProviders.stream()
            .collect(Collectors.toMap(DestinationProvider::getProviderName, this::getIsoCodeToRawDestinations));

    destinationSortingService.triageRawDestinations(providerToIsoToRawDestinationDTOs);
  }

  private List<RawDestinationDTO> getIsoCodeToRawDestinations(DestinationProvider provider) {
    try {

      return provider.getDestinations();

    } catch (Exception e) {
      log.error("Failed to fetch destinations from provider: {}", provider.getProviderName(), e);
      return List.of();
    }
  }

  public List<DestinationDTO> getAutocompleteSuggestions(String query) {

    if (query == null || query.isBlank()) {
      return List.of();
    }

    Locale locale = LocaleContextHolder.getLocale();

    LanguageCode languageCode;
    try {
      languageCode = LanguageCode.from(locale.getCountry());
    } catch (Exception e) {
      languageCode = LanguageCode.EN;
    }
    final LanguageCode finalLanguageCode = languageCode;

    List<Destination> destinations =
        destinationRepository.findCityOrRegionByTranslationsForLanguageCodeAndName(finalLanguageCode, query);

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

              return DestinationDTO.of(destination.getId(), translationName, destination.getType(), countryName);
            })
        .filter(Objects::nonNull)
        .toList();
  }
}
