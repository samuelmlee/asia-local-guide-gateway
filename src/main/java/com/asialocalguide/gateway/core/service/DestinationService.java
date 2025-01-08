package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.domain.*;
import com.asialocalguide.gateway.core.dto.DestinationDTO;
import com.asialocalguide.gateway.core.repository.BookingProviderMappingRepository;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import com.asialocalguide.gateway.viator.exception.ViatorDestinationMappingException;
import com.asialocalguide.gateway.viator.service.ViatorDestinationService;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DestinationService {

  private final ViatorDestinationService viatorDestinationService;

  private final DestinationPersistenceService destinationPersistenceService;

  private final DestinationRepository destinationRepository;

  private final BookingProviderRepository bookingProviderRepository;

  private final BookingProviderMappingRepository bookingProviderMappingRepository;

  private static final String DEFAULT_LANGUAGE_CODE = "en";

  public DestinationService(
      ViatorDestinationService viatorDestinationService,
      DestinationPersistenceService destinationPersistenceService,
      DestinationRepository destinationRepository,
      BookingProviderRepository bookingProviderRepository,
      BookingProviderMappingRepository bookingProviderMappingRepository) {

    this.viatorDestinationService = viatorDestinationService;
    this.destinationPersistenceService = destinationPersistenceService;
    this.destinationRepository = destinationRepository;
    this.bookingProviderRepository = bookingProviderRepository;
    this.bookingProviderMappingRepository = bookingProviderMappingRepository;
  }

  public void syncViatorDestinations() {

    SupportedLocale defaultLocale = getDefaultLocale();

    List<ViatorDestinationDTO> destinations =
        viatorDestinationService.getDestinationDTOs(defaultLocale);

    BookingProvider viatorProvider =
        bookingProviderRepository
            .findByName("VIATOR")
            .orElseThrow(() -> new IllegalStateException("Viator BookingProvider not found"));

    Set<String> viatorDestinationIds =
        bookingProviderMappingRepository.findProviderDestinationIdsByProviderId(
            viatorProvider.getId());

    List<ViatorDestinationDTO> newDestinationDTOs =
        destinations.stream()
            .filter(d -> isNewViatorDestinationDto(d, viatorDestinationIds))
            .collect(Collectors.toCollection(ArrayList::new));

    destinationPersistenceService.buildAndSaveDestinationsFromViatorDtos(newDestinationDTOs);
  }

  private static SupportedLocale getDefaultLocale() {
    return SupportedLocale.stream()
        .filter(SupportedLocale::isDefault)
        .findFirst()
        .orElseThrow(() -> new ViatorDestinationMappingException("No default locale found"));
  }

  private static boolean isNewViatorDestinationDto(
      ViatorDestinationDTO d, Set<String> viatorDestinationIds) {

    // Update method when Destination can be added by other providers to check for name and
    // coordinates

    return !viatorDestinationIds.contains(d.getDestinationId().toString());
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
