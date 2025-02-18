package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.*;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import com.asialocalguide.gateway.core.repository.CountryRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
   * @param idToRawDestinations Map<ProviderName, Map<DestinationId, List<RawDestinationDTO>>>
   */
  @Transactional
  public void persistExistingDestinations(
      BookingProviderName providerName, Map<Long, RawDestinationDTO> idToRawDestinations) {
    if (idToRawDestinations == null || idToRawDestinations.isEmpty()) {
      log.warn("No existing destinations to update.");
      return;
    }

    log.info("Processing existing destinations for provider: {}", providerName);

    BookingProvider provider =
        bookingProviderRepository
            .findByName(providerName)
            .orElseThrow(() -> new IllegalStateException("BookingProvider not found: " + providerName));

    // Fetch all existing Destinations in batch
    List<Destination> existingDestinations = destinationRepository.findAllById(idToRawDestinations.keySet());

    existingDestinations.forEach(
        destination -> {
          RawDestinationDTO rawDto = idToRawDestinations.get(destination.getId());
          if (rawDto == null) {
            log.warn("RawDestinationDTO not found for Destination: {}", destination.getId());
            return;
          }

          if (destination.getBookingProviderMapping(provider.getId()) == null) {
            DestinationProviderMapping mapping = new DestinationProviderMapping();
            mapping.setProvider(provider);
            mapping.setProviderDestinationId(rawDto.destinationId());
            destination.addProviderMapping(mapping);
            log.info("Added provider mapping for Destination {} from {}", destination.getId(), providerName);
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
      BookingProviderName providerName, Map<String, List<RawDestinationDTO>> isoCodeToRawDestinations) {
    if (isoCodeToRawDestinations == null || isoCodeToRawDestinations.isEmpty()) {
      log.warn("No new destinations to persist.");
      return;
    }

    log.info("Processing new destinations for provider: {}", providerName);

    // Extract all country ISO codes
    Set<String> countryIsoCodes = isoCodeToRawDestinations.keySet();

    // Fetch all Countries in batch
    Map<String, Country> countryMap =
        countryRepository.findByIso2CodeIn(countryIsoCodes).stream()
            .collect(Collectors.toMap(Country::getIso2Code, country -> country));

    BookingProvider provider =
        bookingProviderRepository
            .findByName(providerName)
            .orElseThrow(() -> new IllegalStateException("BookingProvider not found: " + providerName));

    List<Destination> newDestinations = new ArrayList<>();

    for (var isoEntry : isoCodeToRawDestinations.entrySet()) {
      String isoCode = isoEntry.getKey();
      List<RawDestinationDTO> rawDestinationDTOs = isoEntry.getValue();

      Country country = countryMap.get(isoCode);
      if (country == null) {
        log.warn("Country not found for ISO Code: {}", isoCode);
        continue;
      }

      for (RawDestinationDTO rawDto : rawDestinationDTOs) {
        Destination newDestination = new Destination();
        newDestination.setCountry(country);
        newDestination.setType(rawDto.type());
        newDestination.setCenterCoordinates(rawDto.centerCoordinates());

        rawDto
            .names()
            .forEach(
                name ->
                    newDestination.addTranslation(
                        new DestinationTranslation(LanguageCode.from(name.languageCode()), name.name())));

        DestinationProviderMapping mapping = new DestinationProviderMapping();
        mapping.setProviderDestinationId(rawDto.destinationId());
        mapping.setProvider(provider);

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
