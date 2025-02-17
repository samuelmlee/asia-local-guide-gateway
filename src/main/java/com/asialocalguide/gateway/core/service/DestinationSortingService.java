package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.Country;
import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.repository.BookingProviderMappingRepository;
import com.asialocalguide.gateway.core.repository.CountryRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

  public void triageRawDestinations(Map<BookingProviderName, List<RawDestinationDTO>> providerToRawDestinations) {
    if (providerToRawDestinations == null || providerToRawDestinations.isEmpty()) {
      log.warn("No raw destinations from any Destination Provider for processing.");
      return;
    }

    log.info("Processing raw destinations from providers: {}", providerToRawDestinations.keySet());

    // Filter out existing destinations for each Provider
    Map<BookingProviderName, List<RawDestinationDTO>> filteredProviderToRawDestinations =
        filterExistingDestinations(providerToRawDestinations);

    // Build Map of ProviderName -> ISO Code -> Raw Destinations
    Map<BookingProviderName, Map<String, List<RawDestinationDTO>>> providerToIsoCodeToRawDestinations =
        groupRawDestinationsByIso(filteredProviderToRawDestinations);

    // Fetch country data
    Map<String, Country> countryMap = buildCountryMap(providerToIsoCodeToRawDestinations);

    // Fetch existing destinations grouped by country
    Map<String, List<Destination>> existingDestinationsByCountry = fetchDestinationsByIsoCode(countryMap.keySet());

    // Maps to store new and existing destinations
    Map<BookingProviderName, Map<String, List<RawDestinationDTO>>> newDestinationsMap =
        new EnumMap<>(BookingProviderName.class);
    Map<BookingProviderName, Map<Long, RawDestinationDTO>> existingDestinationsMap =
        new EnumMap<>(BookingProviderName.class);

    // Process each provider
    providerToIsoCodeToRawDestinations.forEach(
        (provider, isoCodeToRawDestinations) ->
            // Process each ISO Code
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
                }));

    // Save new and updated destinations
    persistDestinations(newDestinationsMap, existingDestinationsMap);
  }

  private Map<BookingProviderName, List<RawDestinationDTO>> filterExistingDestinations(
      Map<BookingProviderName, List<RawDestinationDTO>> providerToRawDestinations) {

    return providerToRawDestinations.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                  BookingProviderName providerName = entry.getKey();
                  List<RawDestinationDTO> destinationDTOs = entry.getValue();

                  Set<String> existingProviderDestinationIds =
                      bookingProviderMappingRepository.findProviderDestinationIdsByProviderName(providerName);

                  return destinationDTOs.stream()
                      .filter(d -> !existingProviderDestinationIds.contains(d.destinationId()))
                      .toList();
                }));
  }

  private static Map<BookingProviderName, Map<String, List<RawDestinationDTO>>> groupRawDestinationsByIso(
      Map<BookingProviderName, List<RawDestinationDTO>> filteredProviderToRawDestinations) {
    return filteredProviderToRawDestinations.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry ->
                    // Return Map Grouped by country iso code
                    entry.getValue().stream().collect(Collectors.groupingBy(RawDestinationDTO::countryIsoCode))));
  }

  private Map<String, Country> buildCountryMap(
      Map<BookingProviderName, Map<String, List<RawDestinationDTO>>> providerToIsoCodeToRawDestinations) {
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

  // TODO: to update when getting Destinations from different providers, starting with destinations of type cities
  // Use approximation of center coordinates and sub string matching for name
  // https://stackoverflow.com/questions/3320698/what-is-the-best-way-to-implement-a-substring-search-in-sql
  private Optional<Destination> findMatchingDestination(
      List<Destination> possibleDestinations, RawDestinationDTO rawDto) {
    return possibleDestinations.stream()
        .filter(dest -> Objects.equals(dest.getCenterCoordinates(), rawDto.centerCoordinates()))
        .findFirst();
  }
}
