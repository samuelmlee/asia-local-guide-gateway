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
      Map<BookingProviderName, Map<String, RawDestinationDTO>> providerToIsoCodeToRawDestinations) {
    if (providerToIsoCodeToRawDestinations == null
        || providerToIsoCodeToRawDestinations.isEmpty()) {
      log.warn("No raw destinations provided for processing.");
      return;
    }

    log.info(
        "Processing raw destinations from providers: {}",
        providerToIsoCodeToRawDestinations.keySet());

    // Extract all country ISO codes
    Set<String> countryIsoCodes =
        providerToIsoCodeToRawDestinations.values().stream()
            .flatMap(providerMap -> providerMap.values().stream())
            .map(RawDestinationDTO::countryIsoCode)
            .collect(Collectors.toSet());

    // Fetch all relevant countries
    Map<String, Country> countryMap =
        countryRepository.findByIso2CodeIn(countryIsoCodes).stream()
            .collect(Collectors.toMap(Country::getIso2Code, country -> country));

    // Fetch all existing destinations grouped by country iso code
    Map<String, List<Destination>> existingDestinationsByCountry =
        destinationRepository.findByCountryIsoCodes(countryIsoCodes).stream()
            .collect(Collectors.groupingBy(dest -> dest.getCountry().getIso2Code()));

    // Prepare data for persistence
    Map<BookingProviderName, Map<String, RawDestinationDTO>> newDestinationsMap =
        new EnumMap<>(BookingProviderName.class);
    Map<BookingProviderName, Map<Long, RawDestinationDTO>> existingDestinationsMap =
        new EnumMap<>(BookingProviderName.class);

    providerToIsoCodeToRawDestinations.forEach(
        (providerName, isoCodeToRawDestinations) ->
            isoCodeToRawDestinations
                .values()
                .forEach(
                    rawDto -> {
                      if (rawDto.destinationId() == null
                          || rawDto.providerType() == null
                          || rawDto.countryIsoCode() == null) {
                        log.warn("Invalid RawDestinationDTO: {}", rawDto);
                        return;
                      }

                      Country country = countryMap.get(rawDto.countryIsoCode());
                      if (country == null) {
                        log.warn("Country not found for ISO Code: {}", rawDto.countryIsoCode());
                        return;
                      }

                      List<Destination> possibleExistingDestinations =
                          existingDestinationsByCountry.getOrDefault(
                              rawDto.countryIsoCode(), new ArrayList<>());

                      findMatchingDestination(possibleExistingDestinations, rawDto)
                          .ifPresentOrElse(
                              existingDestination ->
                                  existingDestinationsMap
                                      .computeIfAbsent(providerName, k -> new HashMap<>())
                                      .put(existingDestination.getId(), rawDto),
                              () ->
                                  newDestinationsMap
                                      .computeIfAbsent(providerName, k -> new HashMap<>())
                                      .put(rawDto.countryIsoCode(), rawDto));
                    }));

    // Save new and updated destinations
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
