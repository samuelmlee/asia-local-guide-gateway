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

  public List<ViatorDestinationDTO> getAllDestinationsForLanguage(String languageCode) {
    try {
      ViatorDestinationResponseDTO destinationResponse =
          viatorRestClient
              .get()
              .uri("/destinations")
              .headers(httpHeaders -> httpHeaders.set("Accept-Language", languageCode))
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

  public List<ViatorActivityDTO> getActivitiesByRequestAndLanguage(
      String languageCode, ViatorActivitySearchDTO searchDTO) {
    try {
      ViatorActivityResponseDTO activityResponse =
          viatorRestClient
              .post()
              .uri("/products/search")
              .headers(httpHeaders -> httpHeaders.set("Accept-Language", languageCode))
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
              .get()
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
