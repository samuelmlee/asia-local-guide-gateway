package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.domain.*;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import com.asialocalguide.gateway.viator.exception.ViatorDestinationMappingException;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ViatorDestinationService {

  private final ViatorClient viatorClient;

  public ViatorDestinationService(ViatorClient viatorClient) {
    this.viatorClient = viatorClient;
  }

  public List<Destination> getAllDestinations() {

    SupportedLocale defaultLocale =
        SupportedLocale.stream().filter(SupportedLocale::isDefault).findFirst().orElse(null);

    List<ViatorDestinationDTO> defaultDestinationDTOs = getDefaultDestinationDTOs(defaultLocale);

    Map<Long, List<DestinationTranslation>> translationsMap = buildAdditionalTranslations();

    return defaultDestinationDTOs.stream()
        .map(dto -> buildDestination(dto, defaultLocale, translationsMap))
        .toList();
  }

  private List<ViatorDestinationDTO> getDefaultDestinationDTOs(SupportedLocale defaultLocale) {

    if (defaultLocale == null) {
      throw new ViatorDestinationMappingException("No default locale found");
    }

    return viatorClient.getAllDestinationsForLocale(defaultLocale.getCode());
  }

  private Map<Long, List<DestinationTranslation>> buildAdditionalTranslations() {

    Map<Long, List<DestinationTranslation>> translationsMap = new HashMap<>();

    List<SupportedLocale> additionalLocales =
        SupportedLocale.stream().filter(locale -> !locale.isDefault()).toList();

    additionalLocales.forEach(
        locale -> {
          List<ViatorDestinationDTO> destinationDTOs =
              viatorClient.getAllDestinationsForLocale(locale.getCode());

          destinationDTOs.forEach(
              dto -> {
                Long destinationId = dto.destinationId();

                DestinationTranslation translation = createTranslations(dto, locale);
                List<DestinationTranslation> translationList =
                    translationsMap.getOrDefault(destinationId, new ArrayList<>());
                translationList.add(translation);
                translationsMap.put(destinationId, translationList);
              });
        });

    return translationsMap;
  }

  private Destination buildDestination(
      ViatorDestinationDTO dto,
      SupportedLocale defaultLocale,
      Map<Long, List<DestinationTranslation>> translationsMap) {

    DestinationTranslation defaultTranslation = createTranslations(dto, defaultLocale);

    List<DestinationTranslation> translationList =
        translationsMap.getOrDefault(dto.destinationId(), new ArrayList<>());
    translationList.add(defaultTranslation);

    BookingProviderMapping bookingProviderMapping =
        BookingProviderMapping.builder()
            .providerDestinationId(dto.destinationId().toString())
            .providerName(BookingProviderName.VIATOR)
            .build();

    return Destination.builder()
        .type(mapToDestinationType(dto.type()))
        .bookingProviderMappings(List.of(bookingProviderMapping))
        .destinationTranslations(translationList)
        .build();
  }

  private DestinationTranslation createTranslations(
      ViatorDestinationDTO viatorDestination, SupportedLocale supportedLocale) {
    return new DestinationTranslation(supportedLocale.getCode(), viatorDestination.name());
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
