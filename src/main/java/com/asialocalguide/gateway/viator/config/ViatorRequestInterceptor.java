package com.asialocalguide.gateway.viator.config;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@Slf4j
class ViatorRequestInterceptor implements ClientHttpRequestInterceptor {
  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

    log.info(
        "Viator Request - Method: {}, URI: {}, Headers: {}",
        request.getMethod(),
        request.getURI(),
        request.getHeaders());

    return execution.execute(request, body);
  }
}
