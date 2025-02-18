package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.Country;
import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.domain.destination.DestinationIngestionInput;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.exception.DestinationIngestionException;
import com.asialocalguide.gateway.core.repository.BookingProviderMappingRepository;
import com.asialocalguide.gateway.core.repository.CountryRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DestinationSortingService {

  private final DestinationRepository destinationRepository;
  private final CountryRepository countryRepository;
  private final BookingProviderMappingRepository bookingProviderMappingRepository;
  private final DestinationPersistenceService destinationPersistenceService;

  public DestinationSortingService(
      DestinationRepository destinationRepository,
      CountryRepository countryRepository,
      BookingProviderMappingRepository bookingProviderMappingRepository,
      DestinationPersistenceService destinationPersistenceService) {
    this.destinationRepository = destinationRepository;
    this.countryRepository = countryRepository;
    this.bookingProviderMappingRepository = bookingProviderMappingRepository;
    this.destinationPersistenceService = destinationPersistenceService;
  }

  public void triageRawDestinations(DestinationIngestionInput ingestionInput) {
    if (ingestionInput == null) {
      throw new DestinationIngestionException("Null DestinationIngestionInput");
    }

    log.info("Processing raw destinations from providers: {}", ingestionInput.providerName());

    BookingProviderName providerName = ingestionInput.providerName();
    List<RawDestinationDTO> rawDestinations = ingestionInput.rawDestinations();

    // Filter out existing destinations for each Provider
    List<RawDestinationDTO> filteredRawDestinations = filterExistingDestinations(providerName, rawDestinations);

    // Build Map of ProviderName -> ISO Code -> Raw Destinations
    Map<String, List<RawDestinationDTO>> isoCodeToRawDestinations = groupRawDestinationsByIso(filteredRawDestinations);

    // Fetch country data
    Map<String, Country> countryMap = buildCountryMap(isoCodeToRawDestinations);

    // Fetch existing destinations grouped by country
    Map<String, List<Destination>> existingDestinationsByCountry = fetchDestinationsByIsoCode(countryMap.keySet());

    // Maps to store new and existing destinations
    Map<String, List<RawDestinationDTO>> newDestinationsMap = new HashMap<>();
    Map<Long, RawDestinationDTO> existingDestinationsMap = new HashMap<>();

    // Process each provider
    processDestinationsByIsoCode(
        isoCodeToRawDestinations,
        countryMap,
        existingDestinationsByCountry,
        newDestinationsMap,
        existingDestinationsMap);

    // Save new and updated destinations
    persistDestinations(newDestinationsMap, existingDestinationsMap);
  }

  private void processDestinationsByIsoCode(
      Map<String, List<RawDestinationDTO>> isoCodeToRawDestinations,
      Map<String, Country> countryMap,
      Map<String, List<Destination>> existingDestinationsByCountry,
      Map<String, List<RawDestinationDTO>> newDestinationsMap,
      Map<Long, RawDestinationDTO> existingDestinationsMap) {

    isoCodeToRawDestinations.forEach(
        (isoCode, rawDestinations) -> {
          // Get country
          Country country = countryMap.get(isoCode);
          if (country == null) {
            log.warn("Country not found for ISO Code: {}", isoCode);
            return;
          }

          // Get possible existing destinations for this country
          List<Destination> possibleExistingDestinations =
              existingDestinationsByCountry.getOrDefault(isoCode, new ArrayList<>());

          // Process destinations
          processRawDestinations(
              provider,
              isoCode,
              rawDestinations,
              possibleExistingDestinations,
              newDestinationsMap,
              existingDestinationsMap);
        });
  }

  private List<RawDestinationDTO> filterExistingDestinations(
      BookingProviderName providerName, List<RawDestinationDTO> rawDestinations) {

    Set<String> existingProviderDestinationIds =
        bookingProviderMappingRepository.findProviderDestinationIdsByProviderName(providerName);

    return rawDestinations.stream().filter(d -> !existingProviderDestinationIds.contains(d.destinationId())).toList();
  }

  private static Map<String, List<RawDestinationDTO>> groupRawDestinationsByIso(
      List<RawDestinationDTO> rawDestinations) {
    // Return Map Grouped by country iso code
    return rawDestinations.stream().collect(Collectors.groupingBy(RawDestinationDTO::countryIsoCode));
  }

  private Map<String, Country> buildCountryMap(Map<String, List<RawDestinationDTO>> isoCodeToRawDestinations) {
    Set<String> countryIsoCodes = isoCodeToRawDestinations.keySet();

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
      if (rawDto == null) {
        log.warn("Null RawDestinationDTO in rawDestinations for provider: {} and iso code : {}", providerName, isoCode);
        continue;
      }

      Optional<Destination> existingDestination = findMatchingDestination(possibleExistingDestinations, rawDto);

      if (existingDestination.isPresent()) {
        existingDestinationsMap
            .computeIfAbsent(providerName, k -> new HashMap<>())
            .put(existingDestination.get().getId(), rawDto);
      } else {
        newDestinationsMap
            .computeIfAbsent(providerName, k -> new HashMap<>())
            .computeIfAbsent(isoCode, k -> new ArrayList<>())
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

  // TODO: to update when getting Destinations from different providers
  // Use rounding of center coordinates / geohash and sub string matching for name for existing destination matching
  // https://www.pubnub.com/guides/what-is-geohashing/
  // https://stackoverflow.com/questions/3320698/what-is-the-best-way-to-implement-a-substring-search-in-sql
  private Optional<Destination> findMatchingDestination(
      List<Destination> possibleDestinations, RawDestinationDTO rawDto) {
    return possibleDestinations.stream()
        .filter(dest -> Objects.equals(dest.getCenterCoordinates(), rawDto.centerCoordinates()))
        .findFirst();
  }
}
