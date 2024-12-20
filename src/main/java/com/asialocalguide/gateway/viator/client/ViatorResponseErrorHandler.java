package com.asialocalguide.gateway.viator.client;

import com.asialocalguide.gateway.viator.exception.ViatorDestinationApiException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class ViatorResponseErrorHandler implements ResponseErrorHandler {

  @Override
  public boolean hasError(ClientHttpResponse response) throws IOException {
    if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
      return false;
    }
    return response.getStatusCode().is4xxClientError()
        || response.getStatusCode().is5xxServerError();
  }

  @Override
  public void handleError(ClientHttpResponse response) throws IOException {

    String responseBody =
        new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"));

    throw new ViatorDestinationApiException(
        String.format(
            "%s status code while calling Destination API: %s - %s - %s",
            response.getStatusCode(),
            response.getStatusCode(),
            response.getHeaders(),
            responseBody));
  }
}
