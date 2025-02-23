package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.*;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import com.asialocalguide.gateway.core.repository.CountryRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
   * @param idToRawDestinations Map<ProviderName, Map<DestinationId, List<RawDestinationDTO>>>
   */
  @Transactional
  public void persistExistingDestinations(
      BookingProviderName providerName, Map<Long, RawDestinationDTO> idToRawDestinations) {
    if (providerName == null || idToRawDestinations == null || idToRawDestinations.isEmpty()) {
      log.warn(
          "Persist existing destinations: BookingProviderName is null or Map<Long, RawDestinationDTO> to process is"
              + " empty.");
      return;
    }

    log.info("Processing existing destinations for provider: {}", providerName);

    BookingProvider provider =
        bookingProviderRepository
            .findByName(providerName)
            .orElseThrow(() -> new IllegalStateException("BookingProvider not found: " + providerName));

    // Fetch all existing Destinations in batch
    List<Destination> existingDestinations = destinationRepository.findAllById(idToRawDestinations.keySet());

    List<Destination> updatedDestinations = new ArrayList<>();

    existingDestinations.forEach(
        destination -> {
          RawDestinationDTO rawDto = idToRawDestinations.get(destination.getId());
          if (rawDto == null) {
            log.warn(
                "RawDestinationDTO not found in idToRawDestinations Map for existing Destination Id to process: {}",
                destination.getId());
            return;
          }

          if (destination.getBookingProviderMapping(provider.getId()) == null) {
            DestinationProviderMapping mapping = new DestinationProviderMapping();
            mapping.setProvider(provider);
            mapping.setProviderDestinationId(rawDto.destinationId());

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
      BookingProviderName providerName, Map<String, List<RawDestinationDTO>> isoCodeToRawDestinations) {
    if (providerName == null || isoCodeToRawDestinations == null || isoCodeToRawDestinations.isEmpty()) {
      log.warn("Abort processing RawDestinationDTO: BookingProviderName is null or List<RawDestinationDTO>> is empty");
      return;
    }

    log.info("Processing new destinations for provider: {}", providerName);

    // Extract all country ISO codes
    Set<String> isoCodes = isoCodeToRawDestinations.keySet();

    // Fetch all Countries in batch
    Map<String, Country> countryMap =
        countryRepository.findByIso2CodeIn(isoCodes).stream()
            .collect(Collectors.toMap(Country::getIso2Code, country -> country));

    BookingProvider provider =
        bookingProviderRepository
            .findByName(providerName)
            .orElseThrow(() -> new IllegalStateException("BookingProvider not found: " + providerName));

    List<Destination> newDestinationList = new ArrayList<>();

    for (var isoEntry : isoCodeToRawDestinations.entrySet()) {

      String isoCode = isoEntry.getKey();
      List<RawDestinationDTO> rawDestinationDTOs = isoEntry.getValue();

      Country country = countryMap.get(isoCode);
      if (country == null) {
        log.warn(
            "Country not found for ISO Code: {}, List<RawDestination> not processed : {}", isoCode, rawDestinationDTOs);
        continue;
      }

      for (RawDestinationDTO rawDto : rawDestinationDTOs) {

        if (rawDto == null) {
          log.warn("Encountered null RawDestinationDTO");
          continue;
        }

        Destination newDestination = new Destination();
        newDestination.setCountry(country);
        newDestination.setType(rawDto.type());
        newDestination.setCenterCoordinates(rawDto.centerCoordinates());

        rawDto
            .names()
            .forEach(
                name ->
                    newDestination.addTranslation(
                        new DestinationTranslation(
                            newDestination, LanguageCode.from(name.languageCode()), name.name())));

        DestinationProviderMapping mapping = new DestinationProviderMapping();
        mapping.setProviderDestinationId(rawDto.destinationId());
        mapping.setProvider(provider);

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
