package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.DestinationType;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.service.composer.DestinationProvider;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import com.asialocalguide.gateway.viator.exception.ViatorApiException;
import java.util.*;
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
  public List<RawDestinationDTO> getDestinations(SupportedLocale locale)
      throws IllegalArgumentException, ViatorApiException {

    if (locale == null) {
      throw new IllegalArgumentException("Locale cannot be null");
    }

    log.info("Fetching Viator destinations for locale: {}", locale.getCode());

    List<ViatorDestinationDTO> destinationDTOs =
        viatorClient.getAllDestinationsForLocale(locale.getCode());

    if (destinationDTOs == null || destinationDTOs.isEmpty()) {
      throw new ViatorApiException("No destinations found for locale: " + locale.getCode());
    }

    return destinationDTOs.stream()
        .map(
            dto -> {
              if (dto == null) {
                log.warn("Encountered a null ViatorDestinationDTO, skipping.");
                return null;
              }

              return new RawDestinationDTO(
                  String.valueOf(dto.destinationId()),
                  dto.name(),
                  mapToDestinationType(dto.type()),
                  dto.coordinates(),
                  PROVIDER_TYPE,
                  locale.getCode());
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
