package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.domain.Destination;
import com.asialocalguide.gateway.core.domain.DestinationType;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ViatorDestinationService {

  private final ViatorClient viatorClient;

  public ViatorDestinationService(ViatorClient viatorClient) {
    this.viatorClient = viatorClient;
  }

  public List<Destination> getAllDestinations() {
    List<ViatorDestinationDTO> viatorDestinations = viatorClient.getAllDestinations();

    return viatorDestinations.stream().map(this::convertToDestination).collect(Collectors.toList());
  }

  private Destination convertToDestination(ViatorDestinationDTO viatorDestinationDTO) {
    return Destination.builder()
        .name(viatorDestinationDTO.name())
        .type(mapToDestinationType(viatorDestinationDTO.type()))
        .build();
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
