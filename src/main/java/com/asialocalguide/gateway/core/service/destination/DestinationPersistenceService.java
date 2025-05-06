package com.asialocalguide.gateway.core.service.destination;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.Language;
import com.asialocalguide.gateway.core.domain.destination.*;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.core.service.LanguageService;
import com.asialocalguide.gateway.core.service.bookingprovider.BookingProviderService;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DestinationPersistenceService {

  private final DestinationRepository destinationRepository;
  private final CountryService countryService;
  private final BookingProviderService bookingProviderService;
  private final LanguageService languageService;

  public DestinationPersistenceService(
      DestinationRepository destinationRepository,
      CountryService countryService,
      BookingProviderService bookingProviderService,
      LanguageService languageService) {
    this.destinationRepository = destinationRepository;
    this.countryService = countryService;
    this.bookingProviderService = bookingProviderService;
    this.languageService = languageService;
  }

  /**
   * Persists existing Destinations by adding a new DestinationProviderMapping for each provider.
   *
   * @param idToRawDestinations Map<ProviderName, Map<DestinationId, List<RawDestinationDTO>>>
   */
  @Transactional
  public void persistExistingDestinations(
      BookingProviderName providerName, Map<Long, CommonDestination> idToRawDestinations) {
    if (providerName == null || idToRawDestinations == null || idToRawDestinations.isEmpty()) {
      log.warn(
          "Persist existing destinations: BookingProviderName is null or Map<Long, RawDestinationDTO> to process is"
              + " empty.");
      return;
    }

    log.info("Processing existing destinations for provider: {}", providerName);

    BookingProvider provider =
        bookingProviderService
            .getBookingProviderByName(providerName)
            .orElseThrow(() -> new IllegalStateException("BookingProvider not found: " + providerName));

    // Fetch all existing Destinations in batch
    List<Destination> existingDestinations = destinationRepository.findAllById(idToRawDestinations.keySet());

    existingDestinations.forEach(
        destination -> {
          CommonDestination rawDto = idToRawDestinations.get(destination.getId());
          if (rawDto == null) {
            log.warn(
                "RawDestinationDTO not found in idToRawDestinations Map for existing Destination Id to process: {}",
                destination.getId());
            return;
          }

          if (destination.getBookingProviderMapping(provider.getId()).isEmpty()) {
            DestinationProviderMapping mapping =
                new DestinationProviderMapping(destination, provider, rawDto.destinationId());

            destination.addProviderMapping(mapping);

            log.info("Added provider mapping for Destination {} from {}", destination.getId(), providerName);
          } else {
            log.warn("Provider mapping already exists for Destination {}, new Mapping : {}", destination, rawDto);
          }
        });

    // Entities are managed, no need to save explicitly
  }

  /**
   * Persists new Destinations and saves them in batch.
   *
   * @param isoCodeToRawDestinations Map<ProviderName, Map<IsoCode, List<RawDestinationDTO>>>
   */
  @Transactional
  public void persistNewDestinations(
      BookingProviderName providerName, Map<String, List<CommonDestination>> isoCodeToRawDestinations) {
    if (providerName == null || isoCodeToRawDestinations == null || isoCodeToRawDestinations.isEmpty()) {
      log.warn("Abort processing RawDestinationDTO: BookingProviderName is null or List<RawDestinationDTO>> is empty");
      return;
    }

    log.info("Processing new destinations for provider: {}", providerName);

    // Extract all country ISO codes
    Set<String> isoCodes = isoCodeToRawDestinations.keySet();

    // Fetch all Countries in batch
    Map<String, Country> countryMap =
        countryService.findByIso2CodeIn(isoCodes).stream()
            .collect(Collectors.toMap(Country::getIso2Code, country -> country));

    BookingProvider provider =
        bookingProviderService
            .getBookingProviderByName(providerName)
            .orElseThrow(() -> new IllegalStateException("BookingProvider not found: " + providerName));

    List<Destination> newDestinationList = new ArrayList<>();

    for (var isoEntry : isoCodeToRawDestinations.entrySet()) {

      String isoCode = isoEntry.getKey();
      List<CommonDestination> commonDestinations = isoEntry.getValue();

      Country country = countryMap.get(isoCode);
      if (country == null) {
        log.warn(
            "Country not found for ISO Code: {}, List<RawDestination> not processed : {}", isoCode, commonDestinations);
        continue;
      }

      List<CommonDestination> nonNullDestinations = commonDestinations.stream().filter(Objects::nonNull).toList();

      Map<LanguageCode, Language> codeToLanguage =
          languageService.getAllLanguages().stream().collect(Collectors.toMap(Language::getCode, Function.identity()));

      for (CommonDestination rawDto : nonNullDestinations) {

        Destination newDestination = new Destination();
        newDestination.setCountry(country);
        newDestination.setType(rawDto.type());
        newDestination.setCenterCoordinates(rawDto.centerCoordinates());

        rawDto
            .names()
            .forEach(
                name ->
                    Optional.ofNullable(codeToLanguage.get(name.languageCode()))
                        .ifPresentOrElse(
                            languageCode ->
                                newDestination.addTranslation(
                                    new DestinationTranslation(newDestination, languageCode, name.name())),
                            () ->
                                log.warn(
                                    "Skipping translation with invalid language code: {} for destination name: {},"
                                        + " destinationId: {}",
                                    name.languageCode(),
                                    name.name(),
                                    rawDto.destinationId())));

        // Destination without valid DestinationTranslation should not be persisted
        if (newDestination.getTranslationCount() == 0) {
          log.warn("No valid translations found for destinationId: {}", rawDto.destinationId());
          continue;
        }

        DestinationProviderMapping mapping =
            new DestinationProviderMapping(newDestination, provider, rawDto.destinationId());

        newDestination.addProviderMapping(mapping);

        newDestinationList.add(newDestination);
      }
    }

    // Save all new Destinations
    if (!newDestinationList.isEmpty()) {
      destinationRepository.saveAll(newDestinationList);
      log.info("Saved {} new destinations", newDestinationList.size());
    }
  }
}
