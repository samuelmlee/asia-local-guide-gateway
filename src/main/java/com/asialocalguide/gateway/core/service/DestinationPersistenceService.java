package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.*;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import com.asialocalguide.gateway.core.repository.CountryRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DestinationPersistenceService {

  private final DestinationRepository destinationRepository;
  private final BookingProviderRepository bookingProviderRepository;
  private final CountryRepository countryRepository;

  public DestinationPersistenceService(
      DestinationRepository destinationRepository,
      BookingProviderRepository bookingProviderRepository,
      CountryRepository countryRepository) {
    this.destinationRepository = destinationRepository;
    this.bookingProviderRepository = bookingProviderRepository;
    this.countryRepository = countryRepository;
  }

  /**
   * Persists existing Destinations by adding a new DestinationProviderMapping for each provider.
   *
   * @param rawDestinations Map<ProviderName, Map<Id, RawDestinationDTO>>
   */
  @Transactional
  public void persistExistingDestinations(
      Map<BookingProviderName, Map<Long, RawDestinationDTO>> rawDestinations) {
    if (rawDestinations == null || rawDestinations.isEmpty()) {
      log.warn("No existing destinations to update.");
      return;
    }

    log.info("Processing existing destinations for providers : {}", rawDestinations.keySet());

    for (var entry : rawDestinations.entrySet()) {
      BookingProviderName providerName = entry.getKey();
      Map<Long, RawDestinationDTO> idToRawDestinations = entry.getValue();

      BookingProvider provider =
          bookingProviderRepository
              .findByName(providerName)
              .orElseThrow(
                  () -> new IllegalStateException("BookingProvider not found: " + providerName));

      // Fetch all existing Destinations to update
      List<Destination> existingDestinations =
          destinationRepository.findAllById(new ArrayList<>(idToRawDestinations.keySet()));

      for (Destination destination : existingDestinations) {
        RawDestinationDTO rawDto = idToRawDestinations.get(destination.getId());

        if (rawDto != null && destination.getBookingProviderMapping(provider.getId()) == null) {
          DestinationProviderMapping mapping = new DestinationProviderMapping();
          mapping.setProvider(provider);
          mapping.setProviderDestinationId(rawDto.destinationId());
          destination.addProviderMapping(mapping);
          log.info(
              "Added provider mapping for Destination {} from {}",
              destination.getId(),
              providerName);
        }
      }

      // Save updated Destinations in batch
      destinationRepository.saveAll(existingDestinations);
    }
  }

  /**
   * Persists new Destinations and saves them in batch
   *
   * @param providerToIsoCodeToRawDestinations Map<ProviderName, Map<IsoCode, RawDestinationDTO>>
   */
  @Transactional
  public void persistNewDestinations(
      Map<BookingProviderName, Map<String, RawDestinationDTO>> providerToIsoCodeToRawDestinations) {
    if (providerToIsoCodeToRawDestinations == null
        || providerToIsoCodeToRawDestinations.isEmpty()) {
      log.warn("No new destinations to persist.");
      return;
    }

    log.info(
        "Processing new destinations for {} providers", providerToIsoCodeToRawDestinations.size());

    // Extract all country ISO codes
    Set<String> countryIsoCodes =
        providerToIsoCodeToRawDestinations.values().stream()
            .flatMap(providerMap -> providerMap.keySet().stream())
            .collect(Collectors.toSet());

    // Fetch all Countries in one go
    Map<String, Country> countryMap =
        countryRepository.findByIso2CodeIn(countryIsoCodes).stream()
            .collect(Collectors.toMap(Country::getIso2Code, country -> country));

    List<Destination> newDestinations = new ArrayList<>();

    for (var entry : providerToIsoCodeToRawDestinations.entrySet()) {
      BookingProviderName providerName = entry.getKey();
      Map<String, RawDestinationDTO> providerDestinations = entry.getValue();

      BookingProvider provider =
          bookingProviderRepository
              .findByName(providerName)
              .orElseThrow(
                  () -> new IllegalStateException("BookingProvider not found: " + providerName));

      for (RawDestinationDTO rawDto : providerDestinations.values()) {
        Country country = countryMap.get(rawDto.countryIsoCode());
        if (country == null) {
          log.warn("Country not found for ISO Code: {}", rawDto.countryIsoCode());
          continue;
        }

        Destination newDestination = new Destination();
        newDestination.setType(rawDto.type());
        newDestination.setCenterCoordinates(rawDto.centerCoordinates());
        newDestination.setCountry(country);

        rawDto
            .names()
            .forEach(
                name ->
                    newDestination.addTranslation(
                        new DestinationTranslation(
                            LanguageCode.from(name.languageCode()), name.name())));

        DestinationProviderMapping mapping = new DestinationProviderMapping();
        mapping.setProvider(provider);
        mapping.setProviderDestinationId(rawDto.destinationId());
        newDestination.addProviderMapping(mapping);

        newDestinations.add(newDestination);
      }
    }

    // Save all new Destinations in batch
    if (!newDestinations.isEmpty()) {
      destinationRepository.saveAll(newDestinations);
      log.info("Saved {} new destinations", newDestinations.size());
    }
  }
}
