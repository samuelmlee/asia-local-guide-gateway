package com.asialocalguide.gateway.viator.client;

import com.asialocalguide.gateway.viator.dto.*;
import com.asialocalguide.gateway.viator.exception.ViatorApiException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

@Component
public class ViatorClient {

  private final RestClient viatorRestClient;

  private final ResponseErrorHandler viatorResponseErrorHandler = new ViatorResponseErrorHandler();

  public ViatorClient(RestClient viatorRestClient) {
    this.viatorRestClient = viatorRestClient;
  }

  public List<ViatorDestinationDTO> getAllDestinationsForLocale(String localeString) {
    try {
      ViatorDestinationResponseDTO destinationResponse =
          viatorRestClient
              .get()
              .uri("/destinations")
              .headers(httpHeaders -> httpHeaders.set("Accept-Language", localeString))
              .retrieve()
              .onStatus(viatorResponseErrorHandler)
              .body(ViatorDestinationResponseDTO.class);

      return Optional.ofNullable(destinationResponse)
          .map(ViatorDestinationResponseDTO::destinations)
          .orElseGet(List::of);

    } catch (ViatorApiException e) {

      throw e;

    } catch (Exception e) {

      throw new ViatorApiException("Failed to call Destination API: " + e.getMessage(), e);
    }
  }

  public List<ViatorActivityTagDTO> getAllActivityTags() {
    try {
      ViatorActivityTagResponseDTO tagResponse =
          viatorRestClient
              .get()
              .uri("/products/tags")
              .retrieve()
              .onStatus(viatorResponseErrorHandler)
              .body(ViatorActivityTagResponseDTO.class);

      return Optional.ofNullable(tagResponse)
          .map(ViatorActivityTagResponseDTO::tags)
          .orElseGet(List::of);

    } catch (ViatorApiException e) {

      throw e;

    } catch (Exception e) {

      throw new ViatorApiException("Failed to call Tags API: " + e.getMessage(), e);
    }
  }

  public List<ViatorActivityDTO> getActivitiesByRequestAndLocale(
      String localeString, ViatorActivitySearchDTO searchDTO) {
    try {
      ViatorActivityResponseDTO activityResponse =
          viatorRestClient
              .post()
              .uri("/products/search")
              .headers(httpHeaders -> httpHeaders.set("Accept-Language", localeString))
              .body(searchDTO)
              .retrieve()
              .onStatus(viatorResponseErrorHandler)
              .body(ViatorActivityResponseDTO.class);

      return Optional.ofNullable(activityResponse)
          .map(ViatorActivityResponseDTO::products)
          .orElseGet(List::of);

    } catch (ViatorApiException e) {

      throw e;

    } catch (Exception e) {

      throw new ViatorApiException("Failed to call Products Search API: " + e.getMessage(), e);
    }
  }

  public Optional<ViatorActivityAvailabilityDTO> getAvailabilityByProductCode(String productCode) {
    try {
      ViatorActivityAvailabilityDTO availability =
          viatorRestClient
              .post()
              .uri("/availability/schedules/{productCode}", productCode)
              .retrieve()
              .onStatus(viatorResponseErrorHandler)
              .body(ViatorActivityAvailabilityDTO.class);

      return Optional.ofNullable(availability);

    } catch (ViatorApiException e) {

      throw e;

    } catch (Exception e) {

      throw new ViatorApiException(
          "Failed to call Availability Schedules API: " + e.getMessage(), e);
    }
  }
}
