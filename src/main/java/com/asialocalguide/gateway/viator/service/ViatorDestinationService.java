package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.domain.*;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import com.asialocalguide.gateway.viator.exception.ViatorDestinationMappingException;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ViatorDestinationService {

  private final ViatorClient viatorClient;

  public ViatorDestinationService(ViatorClient viatorClient) {
    this.viatorClient = viatorClient;
  }

  public List<Destination> getAllDestinations() {

    SupportedLocale defaultLocale = getDefaultLocale();

    Map<Long, ViatorDestinationDTO> defaultDestinationDtoMap =
        getDefaultDestinationDTOs(defaultLocale);

    Map<Long, Set<DestinationTranslation>> translationsMap = buildAdditionalTranslationsMap();

    return defaultDestinationDtoMap.values().stream()
        .map(dto -> buildDestination(dto, defaultDestinationDtoMap, defaultLocale, translationsMap))
        .toList();
  }

  private static SupportedLocale getDefaultLocale() {
    return SupportedLocale.stream()
        .filter(SupportedLocale::isDefault)
        .findFirst()
        .orElseThrow(() -> new ViatorDestinationMappingException("No default locale found"));
  }

  private Map<Long, ViatorDestinationDTO> getDefaultDestinationDTOs(SupportedLocale defaultLocale) {

    List<ViatorDestinationDTO> destinationDTOS =
        viatorClient.getAllDestinationsForLocale(defaultLocale.getCode());

    return destinationDTOS.stream()
        .collect(Collectors.toMap(ViatorDestinationDTO::destinationId, d -> d));
  }

  private Map<Long, Set<DestinationTranslation>> buildAdditionalTranslationsMap() {

    Map<Long, Set<DestinationTranslation>> translationsMap = new HashMap<>();

    List<SupportedLocale> additionalLocales = getAdditionalLocales();

    additionalLocales.forEach(
        locale -> {
          List<ViatorDestinationDTO> destinationDTOs =
              viatorClient.getAllDestinationsForLocale(locale.getCode());

          destinationDTOs.forEach(
              dto -> {
                Long destinationId = dto.destinationId();

                DestinationTranslation translation = createTranslations(dto, locale);

                Set<DestinationTranslation> translationList =
                    translationsMap.getOrDefault(destinationId, new HashSet<>());

                translationList.add(translation);

                translationsMap.put(destinationId, translationList);
              });
        });

    return translationsMap;
  }

  private static List<SupportedLocale> getAdditionalLocales() {
    return SupportedLocale.stream().filter(locale -> !locale.isDefault()).toList();
  }

  private DestinationTranslation createTranslations(
      ViatorDestinationDTO viatorDestination, SupportedLocale supportedLocale) {
    return new DestinationTranslation(supportedLocale.getCode(), viatorDestination.name());
  }

  private Destination buildDestination(
      ViatorDestinationDTO dto,
      Map<Long, ViatorDestinationDTO> defaultDestinationDtoMap,
      SupportedLocale defaultLocale,
      Map<Long, Set<DestinationTranslation>> translationsMap) {

    BookingProviderMapping bookingProviderMapping =
        BookingProviderMapping.builder()
            .providerDestinationId(dto.destinationId().toString())
            .providerName(BookingProviderName.VIATOR)
            .build();

    Destination destination = new Destination();
    destination.setType(mapToDestinationType(dto.type()));
    destination.setBookingProviderMappings(Set.of(bookingProviderMapping));

    Set<DestinationTranslation> translationSet =
        getDestinationTranslations(dto, defaultLocale, translationsMap);

    translationSet.forEach(destination::addTranslation);

    Long parentDestinationId = dto.parentDestinationId();
    if (parentDestinationId != null) {
      ViatorDestinationDTO parentDestination = defaultDestinationDtoMap.get(parentDestinationId);

      //      destination.setParentDestination(parentDestination);
    }

    return destination;
  }

  private Set<DestinationTranslation> getDestinationTranslations(
      ViatorDestinationDTO dto,
      SupportedLocale defaultLocale,
      Map<Long, Set<DestinationTranslation>> translationsMap) {

    Set<DestinationTranslation> translationSet =
        translationsMap.getOrDefault(dto.destinationId(), new HashSet<>());

    DestinationTranslation defaultTranslation = createTranslations(dto, defaultLocale);

    translationSet.add(defaultTranslation);

    return translationSet;
  }

  private DestinationType mapToDestinationType(String viatorType) {
    if (viatorType == null || viatorType.isBlank()) {
      return DestinationType.OTHER;
    }

    return switch (viatorType) {
      case "CITY", "TOWN", "VILLAGE" -> DestinationType.CITY;
      case "COUNTRY" -> DestinationType.COUNTRY;
      case "REGION",
          "AREA",
          "STATE",
          "PROVINCE",
          "COUNTY",
          "DISTRICT",
          "HAMLET",
          "ISLAND",
          "NATIONAL_PARK",
          "NEIGHBORHOOD",
          "PENINSULA",
          "UNION_TERRITORY",
          "WARD" ->
          DestinationType.REGION;
      default -> DestinationType.OTHER;
    };
  }
}
