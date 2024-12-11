package com.asialocalguide.gateway.auxiliary.client;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.asialocalguide.gateway.auxiliary.domain.Destination;
import com.asialocalguide.gateway.auxiliary.dto.DestinationResponseDTO;
import com.asialocalguide.gateway.auxiliary.exception.DestinationApiException;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuxiliaryClient {

  private final RestClient auxiliaryRestClient;

  public AuxiliaryClient(RestClient auxiliaryRestClient) {
    this.auxiliaryRestClient = auxiliaryRestClient;
  }

  public List<Destination> getAllDestinations() {
    DestinationResponseDTO destinationResponse =
        auxiliaryRestClient
            .get()
            .uri("/destinations")
            .accept(APPLICATION_JSON)
            .retrieve()
            .onStatus(
                HttpStatusCode::is4xxClientError,
                (request, response) -> {
                  throw new DestinationApiException(
                      "Client error while calling Destination API: "
                          + response.getStatusCode()
                          + " "
                          + response.getHeaders());
                })
            .onStatus(
                HttpStatusCode::is5xxServerError,
                (request, response) -> {
                  throw new DestinationApiException(
                      "Client error while calling Destination API: "
                          + response.getStatusCode()
                          + " "
                          + response.getHeaders());
                })
            .body(DestinationResponseDTO.class);

    return Optional.ofNullable(destinationResponse)
        .map(DestinationResponseDTO::destinations)
        .orElseGet(List::of);
  }
}
