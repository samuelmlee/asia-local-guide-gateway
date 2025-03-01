package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.CrossPlatformDestination;
import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.domain.destination.DestinationIngestionInput;
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
    Objects.requireNonNull(ingestionInput);
    Objects.requireNonNull(ingestionInput.providerName());
    Objects.requireNonNull(ingestionInput.rawDestinations());

    BookingProviderName providerName = ingestionInput.providerName();
    List<CrossPlatformDestination> rawDestinations = ingestionInput.rawDestinations();

    log.info("Processing raw destinations from provider: {}", providerName);

    // Filter out existing destinations for each Provider
    List<CrossPlatformDestination> filteredRawDestinations = filterExistingDestinations(providerName, rawDestinations);

    // Build Map of ProviderName -> ISO Code -> Raw Destinations
    Map<String, List<CrossPlatformDestination>> isoCodeToRawDestinations =
        groupRawDestinationsByIso(filteredRawDestinations);

    // Get set of ISO codes
    Set<String> supportedIsoCodes = countryRepository.findAllIso2Codes();

    // Fetch existing destinations grouped by country
    Map<String, List<Destination>> isoCodeToExistingDestinations =
        fetchDestinationsByIsoCode(isoCodeToRawDestinations.keySet());

    // Maps to store new and existing destinations
    Map<String, List<CrossPlatformDestination>> newDestinationsMap = new HashMap<>();
    Map<Long, CrossPlatformDestination> existingDestinationsMap = new HashMap<>();

    // Process each provider
    processDestinationsByIsoCode(
        providerName,
        supportedIsoCodes,
        isoCodeToRawDestinations,
        isoCodeToExistingDestinations,
        newDestinationsMap,
        existingDestinationsMap);

    // Save new and updated destinations
    persistDestinations(providerName, newDestinationsMap, existingDestinationsMap);
  }

  private void processDestinationsByIsoCode(
      BookingProviderName providerName,
      Set<String> supportedIsoCodes,
      Map<String, List<CrossPlatformDestination>> isoCodeToRawDestinations,
      Map<String, List<Destination>> isoCodeToExistingDestinations,
      Map<String, List<CrossPlatformDestination>> newDestinationsMap,
      Map<Long, CrossPlatformDestination> existingDestinationsMap) {

    isoCodeToRawDestinations.forEach(
        (isoCode, rawDestinations) -> {
          if (!supportedIsoCodes.contains(isoCode)) {
            log.warn("Unsupported ISO code: {} for provider: {}", isoCode, providerName);
            return;
          }

          // Get possible existing destinations for this country
          List<Destination> possibleExistingDestinations =
              isoCodeToExistingDestinations.getOrDefault(isoCode, new ArrayList<>());

          // Process destinations
          processRawDestinations(
              providerName,
              isoCode,
              rawDestinations,
              possibleExistingDestinations,
              newDestinationsMap,
              existingDestinationsMap);
        });
  }

  private List<CrossPlatformDestination> filterExistingDestinations(
      BookingProviderName providerName, List<CrossPlatformDestination> rawDestinations) {

    Set<String> existingProviderDestinationIds =
        bookingProviderMappingRepository.findProviderDestinationIdsByProviderName(providerName);

    return rawDestinations.stream()
        .filter(Objects::nonNull)
        .filter(d -> !existingProviderDestinationIds.contains(d.destinationId()))
        .toList();
  }

  private static Map<String, List<CrossPlatformDestination>> groupRawDestinationsByIso(
      List<CrossPlatformDestination> rawDestinations) {
    // Return Map Grouped by country iso code
    return rawDestinations.stream()
        .filter(d -> d != null && d.countryIsoCode() != null)
        .collect(Collectors.groupingBy(CrossPlatformDestination::countryIsoCode));
  }

  private Map<String, List<Destination>> fetchDestinationsByIsoCode(Set<String> isoCodes) {
    return destinationRepository.findByIsoCodes(isoCodes).stream()
        .collect(Collectors.groupingBy(dest -> dest.getCountry().getIso2Code()));
  }

  private void processRawDestinations(
      BookingProviderName providerName,
      String isoCode,
      List<CrossPlatformDestination> rawDestinations,
      List<Destination> possibleExistingDestinations,
      Map<String, List<CrossPlatformDestination>> newDestinationsMap,
      Map<Long, CrossPlatformDestination> existingDestinationsMap) {

    for (CrossPlatformDestination rawDto : rawDestinations) {
      if (rawDto == null) {
        log.warn("Null RawDestinationDTO in rawDestinations for provider: {} and iso code : {}", providerName, isoCode);
        continue;
      }

      Optional<Destination> existingDestination = findMatchingDestination(possibleExistingDestinations, rawDto);

      if (existingDestination.isPresent()) {
        existingDestinationsMap.put(existingDestination.get().getId(), rawDto);
      } else {
        newDestinationsMap.computeIfAbsent(isoCode, k -> new ArrayList<>()).add(rawDto);
      }
    }
  }

  private void persistDestinations(
      BookingProviderName providerName,
      Map<String, List<CrossPlatformDestination>> newDestinationsMap,
      Map<Long, CrossPlatformDestination> existingDestinationsMap) {

    if (!newDestinationsMap.isEmpty()) {
      destinationPersistenceService.persistNewDestinations(providerName, newDestinationsMap);
    }

    if (!existingDestinationsMap.isEmpty()) {
      destinationPersistenceService.persistExistingDestinations(providerName, existingDestinationsMap);
    }
  }

  // TODO: to update when getting Destinations from different providers
  // Use rounding of center coordinates / geohash AND sub string matching for name for existing destination matching
  // https://www.pubnub.com/guides/what-is-geohashing/
  // https://stackoverflow.com/questions/3320698/what-is-the-best-way-to-implement-a-substring-search-in-sql
  private Optional<Destination> findMatchingDestination(
      List<Destination> possibleDestinations, CrossPlatformDestination rawDto) {
    return possibleDestinations.stream()
        .filter(
            dest -> {
              if (dest == null
                  || dest.getCenterCoordinates() == null
                  || rawDto == null
                  || rawDto.centerCoordinates() == null) {
                return false;
              }

              return Objects.equals(dest.getCenterCoordinates(), rawDto.centerCoordinates());
            })
        .findFirst();
  }
}
