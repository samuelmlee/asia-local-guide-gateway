package com.asialocalguide.gateway.viator.config;

import java.awt.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class ViatorConfig {

  private final ViatorProperties viatorProperties;

  public ViatorConfig(ViatorProperties viatorProperties) {
    this.viatorProperties = viatorProperties;
  }

  @Bean
  public RestClient viatorRestClient() {

    return RestClient.builder()
        .baseUrl(viatorProperties.baseUrl())
        .defaultHeaders(
            headers -> {
              headers.set(viatorProperties.authHeader(), viatorProperties.apiKey());
              headers.set(
                  "Accept", MediaType.APPLICATION_JSON + ";" + viatorProperties.apiVersion());
            })
        .build();
  }
}
