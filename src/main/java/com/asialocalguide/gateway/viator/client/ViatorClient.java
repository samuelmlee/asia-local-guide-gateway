package com.asialocalguide.gateway.viator.client;

import com.asialocalguide.gateway.viator.dto.*;
import com.asialocalguide.gateway.viator.exception.ViatorApiException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ViatorClient {

  private final RestClient viatorRestClient;

  public ViatorClient(RestClient viatorRestClient) {
    this.viatorRestClient = viatorRestClient;
  }

  public List<ViatorDestinationDTO> getAllDestinationsForLanguage(String languageIsoCode) {
    try {
      ResponseEntity<ViatorDestinationResponseDTO> entity =
          viatorRestClient
              .get()
              .uri("/destinations")
              .headers(httpHeaders -> httpHeaders.set("Accept-Language", languageIsoCode))
              .retrieve()
              .onStatus(
                  // Exclude 404 from errors
                  status -> (status.is4xxClientError() && status != HttpStatus.NOT_FOUND) || status.is5xxServerError(),
                  (req, res) -> handleViatorError(res))
              .toEntity(ViatorDestinationResponseDTO.class);

      if (entity.getStatusCode() == HttpStatus.NOT_FOUND) {
        return List.of();
      }

      return Optional.ofNullable(entity.getBody()).map(ViatorDestinationResponseDTO::destinations).orElseGet(List::of);

    } catch (Exception e) {
      if (e instanceof ViatorApiException) {
        throw e;
      }

      throw new ViatorApiException("Failed to call Destination API: " + e.getMessage(), e);
    }
  }

  public List<ViatorActivityDTO> getActivitiesByRequestAndLanguage(
      String languageIsoCode, ViatorActivitySearchDTO searchDTO) {
    try {
      ResponseEntity<ViatorActivityResponseDTO> entity =
          viatorRestClient
              .post()
              .uri("/products/search")
              .headers(httpHeaders -> httpHeaders.set("Accept-Language", languageIsoCode))
              .body(searchDTO)
              .retrieve()
              .onStatus(
                  // Exclude 404 from errors
                  status -> (status.is4xxClientError() && status != HttpStatus.NOT_FOUND) || status.is5xxServerError(),
                  (req, res) -> handleViatorError(res))
              .toEntity(ViatorActivityResponseDTO.class);

      if (entity.getStatusCode() == HttpStatus.NOT_FOUND) {
        return List.of();
      }

      return Optional.ofNullable(entity.getBody()).map(ViatorActivityResponseDTO::products).orElseGet(List::of);

    } catch (Exception e) {
      if (e instanceof ViatorApiException) {
        throw e;
      }

      throw new ViatorApiException("Failed to call Products Search API: " + e.getMessage(), e);
    }
  }

  public Optional<ViatorActivityAvailabilityDTO> getAvailabilityByProductCode(String productCode) {
    try {
      ResponseEntity<ViatorActivityAvailabilityDTO> entity =
          viatorRestClient
              .get()
              .uri("/availability/schedules/{productCode}", productCode)
              .retrieve()
              .onStatus(
                  // Exclude 404 from errors
                  status -> (status.is4xxClientError() && status != HttpStatus.NOT_FOUND) || status.is5xxServerError(),
                  (req, res) -> handleViatorError(res))
              .toEntity(ViatorActivityAvailabilityDTO.class);

      if (entity.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }

      return Optional.ofNullable(entity.getBody());

    } catch (Exception e) {
      if (e instanceof ViatorApiException) {
        throw e;
      }

      throw new ViatorApiException("Failed to call Availability Schedules API: " + e.getMessage(), e);
    }
  }

  private void handleViatorError(ClientHttpResponse response) throws IOException {
    String responseBody =
        new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"));
    throw new ViatorApiException("Viator API error: " + response.getStatusCode() + " - " + responseBody);
  }
}
