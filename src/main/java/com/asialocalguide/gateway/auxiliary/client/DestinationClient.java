package com.asialocalguide.gateway.auxiliary.client;

import com.asialocalguide.gateway.auxiliary.domain.Destination;
import com.asialocalguide.gateway.auxiliary.dto.DestinationResponseDTO;
import com.asialocalguide.gateway.auxiliary.exception.DestinationApiException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DestinationClient {

  private final RestClient auxiliaryRestClient;

  public DestinationClient(RestClient auxiliaryRestClient) {
    this.auxiliaryRestClient = auxiliaryRestClient;
  }

  public List<Destination> getAllDestinations() {
    try {
      DestinationResponseDTO destinationResponse =
          auxiliaryRestClient
              .get()
              .uri("/destinations")
              .headers(
                  httpHeaders -> {
                    httpHeaders.set("Accept", "application/json;version=2.0");
                    httpHeaders.set("Accept-Language", "fr");
                  })
              .retrieve()
              .onStatus(
                  HttpStatusCode::is4xxClientError,
                  (request, response) -> handleError(response, "Client"))
              .onStatus(
                  HttpStatusCode::is5xxServerError,
                  (request, response) -> handleError(response, "Server"))
              .body(DestinationResponseDTO.class);

      return Optional.ofNullable(destinationResponse)
          .map(DestinationResponseDTO::destinations)
          .orElseGet(List::of);

    } catch (DestinationApiException e) {

      throw e;

    } catch (Exception e) {

      throw new DestinationApiException("Failed to call Destination API: " + e.getMessage(), e);
    }
  }

  private void handleError(ClientHttpResponse response, String source) throws IOException {
    String responseBody =
        new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"));

    throw new DestinationApiException(
        String.format(
            "%s error while calling Destination API: %s - %s - %s",
            source, response.getStatusCode(), response.getHeaders(), responseBody));
  }
}
