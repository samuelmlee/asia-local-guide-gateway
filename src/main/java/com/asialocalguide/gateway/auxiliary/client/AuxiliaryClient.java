package com.asialocalguide.gateway.auxiliary.client;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.asialocalguide.gateway.auxiliary.domain.Destination;
import com.asialocalguide.gateway.auxiliary.dto.DestinationResponseDTO;
import com.asialocalguide.gateway.auxiliary.exception.DestinationApiException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuxiliaryClient {

  private final RestClient auxiliaryRestClient;

  public AuxiliaryClient(RestClient auxiliaryRestClient) {
    this.auxiliaryRestClient = auxiliaryRestClient;
  }

  public List<Destination> getAllDestinations() {
    try {
      DestinationResponseDTO destinationResponse =
          auxiliaryRestClient
              .get()
              .uri("/destinations")
              .accept(APPLICATION_JSON)
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
    throw new DestinationApiException(
        source
            + " error while calling Destination API: "
            + response.getStatusCode()
            + "-"
            + response.getHeaders()
            + "-"
            + response.getBody());
  }
}
