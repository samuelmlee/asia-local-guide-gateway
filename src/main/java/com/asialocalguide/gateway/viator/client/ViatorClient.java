package com.asialocalguide.gateway.viator.client;

import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationResponseDTO;
import com.asialocalguide.gateway.viator.exception.ViatorDestinationApiException;
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
public class ViatorClient {

  private final RestClient viatorClient;

  public ViatorClient(RestClient viatorClient) {
    this.viatorClient = viatorClient;
  }

  public List<ViatorDestinationDTO> getAllDestinationsForLocale(String localeString) {
    try {
      ViatorDestinationResponseDTO destinationResponse =
          viatorClient
              .get()
              .uri("/destinations")
              .headers(
                  httpHeaders -> {
                    httpHeaders.set("Accept", "application/json;version=2.0");
                    httpHeaders.set("Accept-Language", localeString);
                  })
              .retrieve()
              .onStatus(
                  HttpStatusCode::is4xxClientError,
                  (request, response) -> handleError(response, "Client"))
              .onStatus(
                  HttpStatusCode::is5xxServerError,
                  (request, response) -> handleError(response, "Server"))
              .body(ViatorDestinationResponseDTO.class);

      return Optional.ofNullable(destinationResponse)
          .map(ViatorDestinationResponseDTO::viatorDestinationDTOS)
          .orElseGet(List::of);

    } catch (ViatorDestinationApiException e) {

      throw e;

    } catch (Exception e) {

      throw new ViatorDestinationApiException(
          "Failed to call Destination API: " + e.getMessage(), e);
    }
  }

  private void handleError(ClientHttpResponse response, String source) throws IOException {
    String responseBody =
        new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"));

    throw new ViatorDestinationApiException(
        String.format(
            "%s error while calling Destination API: %s - %s - %s",
            source, response.getStatusCode(), response.getHeaders(), responseBody));
  }
}
