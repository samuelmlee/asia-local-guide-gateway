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

  private final ViatorTranslationService viatorTranslationService;

  public ViatorDestinationService(
      ViatorClient viatorClient, ViatorTranslationService viatorTranslationService) {
    this.viatorClient = viatorClient;
    this.viatorTranslationService = viatorTranslationService;
  }

  public List<Destination> getAllDestinations() {

    SupportedLocale defaultLocale = getDefaultLocale();

    List<ViatorDestinationDTO> destinationDtoMap = getDefaultDestinationDTOs(defaultLocale);

    List<Destination> destinations =
        destinationDtoMap.values().stream()
            .map(dto -> buildDestination(dto, defaultLocale))
            .toList();

    return destinations;
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
        .collect(Collectors.toMap(ViatorDestinationDTO::getDestinationId, d -> d));
  }

  private Destination buildDestination(ViatorDestinationDTO dto, SupportedLocale defaultLocale) {

    BookingProviderMapping bookingProviderMapping =
        BookingProviderMapping.builder()
            .providerDestinationId(dto.getDestinationId().toString())
            .providerName(BookingProviderName.VIATOR)
            .build();

    Destination destination = new Destination();
    destination.setType(mapToDestinationType(dto.getType()));
    destination.setBookingProviderMappings(Set.of(bookingProviderMapping));

    DestinationTranslation translation =
        viatorTranslationService.createTranslation(dto, defaultLocale);
    destination.addTranslation(translation);

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
