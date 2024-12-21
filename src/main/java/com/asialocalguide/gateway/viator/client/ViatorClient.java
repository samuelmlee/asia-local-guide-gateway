package com.asialocalguide.gateway.viator.client;

import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationResponseDTO;
import com.asialocalguide.gateway.viator.exception.ViatorDestinationApiException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

@Component
public class ViatorClient {

  private final RestClient viatorClient;

  private final ResponseErrorHandler viatorResponseErrorHandler = new ViatorResponseErrorHandler();

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
                    httpHeaders.set("Accept-Language", localeString);
                  })
              .retrieve()
              .onStatus(viatorResponseErrorHandler)
              .body(ViatorDestinationResponseDTO.class);

      return Optional.ofNullable(destinationResponse)
          .map(ViatorDestinationResponseDTO::destinations)
          .orElseGet(List::of);

    } catch (ViatorDestinationApiException e) {

      throw e;

    } catch (Exception e) {

      throw new ViatorDestinationApiException(
          "Failed to call Destination API: " + e.getMessage(), e);
    }
  }
}
