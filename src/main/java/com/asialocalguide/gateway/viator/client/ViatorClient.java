package com.asialocalguide.gateway.viator.client;

import com.asialocalguide.gateway.viator.dto.ViatorActivityTagDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityTagResponseDTO;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationResponseDTO;
import com.asialocalguide.gateway.viator.exception.ViatorActivityTagApiException;
import com.asialocalguide.gateway.viator.exception.ViatorDestinationApiException;
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

    } catch (ViatorDestinationApiException e) {

      throw e;

    } catch (Exception e) {

      throw new ViatorDestinationApiException(
          "Failed to call Destination API: " + e.getMessage(), e);
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

    } catch (ViatorActivityTagApiException e) {

      throw e;

    } catch (Exception e) {

      throw new ViatorActivityTagApiException("Failed to call Tags API: " + e.getMessage(), e);
    }
  }
}
