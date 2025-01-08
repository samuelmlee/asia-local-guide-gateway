package com.asialocalguide.gateway.core.converter;

import com.asialocalguide.gateway.core.domain.Destination;
import com.asialocalguide.gateway.core.domain.DestinationType;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ViatorDestinationToDestinationConverter
    implements Converter<ViatorDestinationDTO, Destination> {

  @Override
  public Destination convert(ViatorDestinationDTO dto) {
    Destination destination = new Destination();
    destination.setType(mapToDestinationType(dto.getType()));

    return destination;
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
