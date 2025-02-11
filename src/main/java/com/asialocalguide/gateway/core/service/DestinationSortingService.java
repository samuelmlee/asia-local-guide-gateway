package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.*;
import com.asialocalguide.gateway.core.domain.destination.*;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.repository.CountryRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DestinationSortingService {

  private final DestinationRepository destinationRepository;
  private final CountryRepository countryRepository;
  private final DestinationPersistenceService destinationPersistenceService;

  public DestinationSortingService(
      DestinationRepository destinationRepository,
      CountryRepository countryRepository,
      DestinationPersistenceService destinationPersistenceService) {
    this.destinationRepository = destinationRepository;
    this.countryRepository = countryRepository;
    this.destinationPersistenceService = destinationPersistenceService;
  }

  public void triageRawDestinations(
      Map<BookingProviderName, Map<String, List<RawDestinationDTO>>>
          providerToIsoCodeToRawDestinations) {
    if (providerToIsoCodeToRawDestinations == null
        || providerToIsoCodeToRawDestinations.isEmpty()) {
      log.warn("No raw destinations provided for processing.");
      return;
    }

    log.info(
        "Processing raw destinations from providers: {}",
        providerToIsoCodeToRawDestinations.keySet());

    // Fetch country data
    Map<String, Country> countryMap = buildCountryMap(providerToIsoCodeToRawDestinations);

    // Fetch existing destinations grouped by country
    Map<String, List<Destination>> existingDestinationsByCountry =
        fetchDestinationsByIsoCode(countryMap.keySet());

    // Maps to store new and existing destinations
    Map<BookingProviderName, Map<String, List<RawDestinationDTO>>> newDestinationsMap =
        new EnumMap<>(BookingProviderName.class);
    Map<BookingProviderName, Map<Long, RawDestinationDTO>> existingDestinationsMap =
        new EnumMap<>(BookingProviderName.class);

    // Process each provider
    for (var providerEntry : providerToIsoCodeToRawDestinations.entrySet()) {
      BookingProviderName providerName = providerEntry.getKey();
      Map<String, List<RawDestinationDTO>> isoCodeToRawDestinations = providerEntry.getValue();

      // Process each ISO Code
      for (var isoEntry : isoCodeToRawDestinations.entrySet()) {
        String isoCode = isoEntry.getKey();
        List<RawDestinationDTO> rawDestinations = isoEntry.getValue();

        // Get country
        Country country = countryMap.get(isoCode);
        if (country == null) {
          log.warn("Country not found for ISO Code: {}", isoCode);
          continue;
        }

        // Get possible existing destinations for this country
        List<Destination> possibleExistingDestinations =
            existingDestinationsByCountry.getOrDefault(isoCode, new ArrayList<>());

        // Process destinations
        processRawDestinations(
            providerName,
            isoCode,
            rawDestinations,
            possibleExistingDestinations,
            newDestinationsMap,
            existingDestinationsMap);
      }
    }

    // Save new and updated destinations
    persistDestinations(newDestinationsMap, existingDestinationsMap);
  }

  private Map<String, Country> buildCountryMap(
      Map<BookingProviderName, Map<String, List<RawDestinationDTO>>>
          providerToIsoCodeToRawDestinations) {
    Set<String> countryIsoCodes =
        providerToIsoCodeToRawDestinations.values().stream()
            .flatMap(map -> map.keySet().stream())
            .collect(Collectors.toSet());

    return countryRepository.findByIso2CodeIn(countryIsoCodes).stream()
        .collect(Collectors.toMap(Country::getIso2Code, country -> country));
  }

  private Map<String, List<Destination>> fetchDestinationsByIsoCode(Set<String> countryIsoCodes) {
    return destinationRepository.findByCountryIsoCodes(countryIsoCodes).stream()
        .collect(Collectors.groupingBy(dest -> dest.getCountry().getIso2Code()));
  }

  private void processRawDestinations(
      BookingProviderName providerName,
      String isoCode,
      List<RawDestinationDTO> rawDestinations,
      List<Destination> possibleExistingDestinations,
      Map<BookingProviderName, Map<String, List<RawDestinationDTO>>> newDestinationsMap,
      Map<BookingProviderName, Map<Long, RawDestinationDTO>> existingDestinationsMap) {

    for (RawDestinationDTO rawDto : rawDestinations) {
      if (rawDto.destinationId() == null
          || rawDto.providerType() == null
          || rawDto.countryIsoCode() == null) {
        log.warn("Invalid RawDestinationDTO: {}", rawDto);
        continue;
      }

      Optional<Destination> existingDestination =
          findMatchingDestination(possibleExistingDestinations, rawDto);

      if (existingDestination.isPresent()) {
        existingDestinationsMap
            .getOrDefault(providerName, new HashMap<>())
            .put(existingDestination.get().getId(), rawDto);
      } else {
        newDestinationsMap
            .getOrDefault(providerName, new HashMap<>())
            .getOrDefault(isoCode, new ArrayList<>())
            .add(rawDto);
      }
    }
  }

  private void persistDestinations(
      Map<BookingProviderName, Map<String, List<RawDestinationDTO>>> newDestinationsMap,
      Map<BookingProviderName, Map<Long, RawDestinationDTO>> existingDestinationsMap) {

    if (!newDestinationsMap.isEmpty()) {
      destinationPersistenceService.persistNewDestinations(newDestinationsMap);
    }

    if (!existingDestinationsMap.isEmpty()) {
      destinationPersistenceService.persistExistingDestinations(existingDestinationsMap);
    }
  }

  // TODO: to update when getting Destinations from different providers, starting with cities
  // Use approximation of coordinates and sub string matching for name
  // https://stackoverflow.com/questions/3320698/what-is-the-best-way-to-implement-a-substring-search-in-sql
  private Optional<Destination> findMatchingDestination(
      List<Destination> possibleDestinations, RawDestinationDTO rawDto) {
    return possibleDestinations.stream()
        .filter(dest -> Objects.equals(dest.getCenterCoordinates(), rawDto.centerCoordinates()))
        .findFirst();
  }
}
