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

  private final ViatorTranslationService viatorTranslationService;

  public ViatorDestinationService(
      ViatorClient viatorClient, ViatorTranslationService viatorTranslationService) {
    this.viatorClient = viatorClient;
    this.viatorTranslationService = viatorTranslationService;
  }

  public List<Destination> getAllDestinations() {

    SupportedLocale defaultLocale = getDefaultLocale();

    List<ViatorDestinationDTO> defaultDestinationDTOs = getDefaultDestinationDTOs(defaultLocale);

    defaultDestinationDTOs.sort(Comparator.comparing(dto -> dto.getLookupIds().size()));

    Map<Long, Destination> createdDestinations = new HashMap<>();

    return defaultDestinationDTOs.stream()
        .map(dto -> buildDestination(dto, createdDestinations, defaultLocale))
        .toList();
  }

  private static SupportedLocale getDefaultLocale() {
    return SupportedLocale.stream()
        .filter(SupportedLocale::isDefault)
        .findFirst()
        .orElseThrow(() -> new ViatorDestinationMappingException("No default locale found"));
  }

  private List<ViatorDestinationDTO> getDefaultDestinationDTOs(SupportedLocale defaultLocale) {

    return viatorClient.getAllDestinationsForLocale(defaultLocale.getCode());
  }

  private Destination buildDestination(
      ViatorDestinationDTO dto,
      Map<Long, Destination> createdDestinations,
      SupportedLocale defaultLocale) {

    Long destinationId = dto.getDestinationId();

    BookingProviderMapping bookingProviderMapping =
        BookingProviderMapping.builder()
            .providerDestinationId(String.valueOf(destinationId))
            .providerName(BookingProviderName.VIATOR)
            .build();

    Destination destination = new Destination();
    destination.setType(mapToDestinationType(dto.getType()));
    destination.setBookingProviderMappings(Set.of(bookingProviderMapping));

    DestinationTranslation translation =
        viatorTranslationService.createTranslation(dto, defaultLocale);
    destination.addTranslation(translation);

    Destination parentDestination = resolveParentDestination(dto, createdDestinations);
    destination.setParentDestination(parentDestination);

    createdDestinations.put(destinationId, destination);

    return destination;
  }

  private Destination resolveParentDestination(
      ViatorDestinationDTO dto, Map<Long, Destination> createdDestinations) {

    List<Long> lookupIds = dto.getLookupIds();

    for (Long parentId : lookupIds) {
      Destination parent = createdDestinations.get(parentId);
      if (parent != null && parent.getType() == DestinationType.COUNTRY) {
        return parent;
      }
    }
    return null;
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
