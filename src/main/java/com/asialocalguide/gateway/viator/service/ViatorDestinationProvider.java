package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.DestinationType;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.service.composer.DestinationProvider;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import com.asialocalguide.gateway.viator.exception.ViatorApiException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ViatorDestinationProvider implements DestinationProvider {

  private static final BookingProviderName PROVIDER_TYPE = BookingProviderName.VIATOR;
  private final ViatorClient viatorClient;

  public ViatorDestinationProvider(ViatorClient viatorClient) {
    this.viatorClient = viatorClient;
  }

  @Override
  public BookingProviderName getProviderType() {
    return PROVIDER_TYPE;
  }

  @Override
  public List<RawDestinationDTO> getDestinations() throws ViatorApiException {
    log.info(
        "Fetching Viator destinations for languages: {}", Arrays.toString(LanguageCode.values()));

    Map<LanguageCode, Map<Long, ViatorDestinationDTO>> languageToDestinations =
        new EnumMap<>(LanguageCode.class);

    for (LanguageCode language : LanguageCode.values()) {
      List<ViatorDestinationDTO> destinations =
          viatorClient.getAllDestinationsForLanguage(language.toString());

      if (destinations == null || destinations.isEmpty()) {
        throw new ViatorApiException(
            String.format("No destinations found for language: %s aborting ingestion.", language));
      }

      languageToDestinations.put(
          language,
          destinations.stream()
              .collect(Collectors.toMap(ViatorDestinationDTO::destinationId, Function.identity())));
    }

    Map<Long, ViatorDestinationDTO> idToDestinationEnDTOs =
        languageToDestinations.get(LanguageCode.EN);

    return idToDestinationEnDTOs.values().stream()
        // The app does not support scheduling activities within a whole country
        .filter(d -> d != null && !"COUNTRY".equals(d.type()) && d.lookupIds() != null)
        .map(dto -> createRawDestinationDTO(dto, languageToDestinations))
        .flatMap(Optional::stream)
        .toList();
  }

  private Optional<RawDestinationDTO> createRawDestinationDTO(
      ViatorDestinationDTO dto,
      Map<LanguageCode, Map<Long, ViatorDestinationDTO>> languageToDestinations) {

    ViatorDestinationDTO country =
        resolveDestinationCountry(dto, languageToDestinations.get(LanguageCode.EN)).orElse(null);

    if (country == null) {
      log.warn("Skipping destination {} due to missing country.", dto.destinationId());
      return Optional.empty();
    }

    return Optional.of(
        new RawDestinationDTO(
            String.valueOf(dto.destinationId()),
            resolveTranslations(dto.destinationId(), languageToDestinations),
            mapToDestinationType(dto.type()),
            dto.coordinates(),
            PROVIDER_TYPE,
            country.name(),
            country.countryCallingCode(),
            null));
  }

  private List<RawDestinationDTO.Translation> resolveTranslations(
      Long destinationId,
      Map<LanguageCode, Map<Long, ViatorDestinationDTO>> languageToDestinations) {

    return languageToDestinations.entrySet().stream()
        .map(
            entry -> {
              ViatorDestinationDTO dto = entry.getValue().get(destinationId);
              if (dto == null) {
                log.debug(
                    "No translation found for destinationId: {} in language: {}",
                    destinationId,
                    entry.getKey());
                return null;
              }
              return new RawDestinationDTO.Translation(entry.getKey().toString(), dto.name());
            })
        .filter(Objects::nonNull)
        .toList();
  }

  private DestinationType mapToDestinationType(String viatorType) {
    if (viatorType == null || viatorType.isBlank()) {
      return DestinationType.OTHER;
    }

    return switch (viatorType) {
      case "CITY", "TOWN", "VILLAGE" -> DestinationType.CITY;
      case "REGION",
          "AREA",
          "STATE",
          "PROVINCE",
          "COUNTY",
          "HAMLET",
          "ISLAND",
          "NATIONAL_PARK",
          "PENINSULA",
          "UNION_TERRITORY" ->
          DestinationType.REGION;
      case "DISTRICT", "NEIGHBORHOOD", "WARD" -> DestinationType.DISTRICT;
      default -> DestinationType.OTHER;
    };
  }

  private Optional<ViatorDestinationDTO> resolveDestinationCountry(
      ViatorDestinationDTO dto, Map<Long, ViatorDestinationDTO> idToDestination) {

    return dto.lookupIds().stream()
        .map(idToDestination::get)
        .filter(d -> d != null && "COUNTRY".equals(d.type()))
        .findFirst();
  }
}
