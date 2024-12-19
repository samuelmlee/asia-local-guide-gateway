package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.domain.Destination;
import com.asialocalguide.gateway.core.domain.DestinationTranslation;
import com.asialocalguide.gateway.core.domain.DestinationType;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ViatorDestinationService {

  private final ViatorClient viatorClient;

  public ViatorDestinationService(ViatorClient viatorClient) {
    this.viatorClient = viatorClient;
  }

  public List<Destination> getAllDestinations() {
    List<Destination> allDestinations = new ArrayList<>();

    for (SupportedLocale supportedLocale : SupportedLocale.values()) {
      List<ViatorDestinationDTO> viatorDestinations =
          viatorClient.getAllDestinationsForLocale(supportedLocale.getCode());

      allDestinations.addAll(
          viatorDestinations.stream()
              .map(
                  viatorDestinationDTO ->
                      convertToDestination(viatorDestinationDTO, supportedLocale))
              .toList());
    }

    return allDestinations;
  }

  private Destination convertToDestination(
      ViatorDestinationDTO viatorDestinationDTO, SupportedLocale supportedLocale) {

    List<DestinationTranslation> translations =
        createTranslations(viatorDestinationDTO, supportedLocale);

    return Destination.builder()
        .type(mapToDestinationType(viatorDestinationDTO.type()))
        .destinationTranslations(translations)
        .build();
  }

  private List<DestinationTranslation> createTranslations(
      ViatorDestinationDTO viatorDestinationDTO, SupportedLocale supportedLocale) {
    DestinationTranslation translation =
        new DestinationTranslation(supportedLocale.getCode(), viatorDestinationDTO.name());

    return List.of(translation);
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
