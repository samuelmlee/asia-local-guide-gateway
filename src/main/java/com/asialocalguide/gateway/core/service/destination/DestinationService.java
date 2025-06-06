package com.asialocalguide.gateway.core.service.destination;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.CommonDestination;
import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.domain.destination.DestinationIngestionInput;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.dto.destination.DestinationDTO;
import com.asialocalguide.gateway.core.exception.DestinationIngestionException;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.core.service.composer.DestinationProvider;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

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

  public Optional<Destination> findDestinationById(UUID id) {
    return destinationRepository.findById(id);
  }

  public void syncDestinationsForProvider(BookingProviderName providerName) {

    Objects.requireNonNull(providerName);

    DestinationProvider destinationProvider =
        destinationProviders.stream()
            .filter(provider -> provider.getProviderName().equals(providerName))
            .findFirst()
            .orElseThrow(() -> new DestinationIngestionException("Invalid provider name: " + providerName));

    List<CommonDestination> rawDestinations = destinationProvider.getDestinations();

    DestinationIngestionInput input = new DestinationIngestionInput(providerName, rawDestinations);

    destinationSortingService.triageRawDestinations(input);
  }

  public List<DestinationDTO> getAutocompleteSuggestions(String query) {

    if (query == null || query.isBlank()) {
      return List.of();
    }

    Locale locale = LocaleContextHolder.getLocale();

    // Default to English if the locale is not supported
    LanguageCode languageCode = LanguageCode.from(locale.getLanguage()).orElse(LanguageCode.EN);

    List<Destination> destinations =
        destinationRepository.findCityOrRegionByNameWithEagerTranslations(languageCode, query);

    return destinations.stream()
        .map(
            destination -> {
              String translationName = destination.getTranslation(languageCode).orElse("");

              if (translationName.isEmpty()) {
                return null;
              }

              String countryName =
                  destination.getCountry() != null
                      ? destination.getCountry().getTranslation(languageCode).orElse("")
                      : "";

              return DestinationDTO.of(destination.getId(), translationName, destination.getType(), countryName);
            })
        .filter(Objects::nonNull)
        .toList();
  }
}
