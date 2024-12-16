package com.asialocalguide.gateway.viator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        .defaultHeader(viatorProperties.authHeader(), viatorProperties.apiKey())
        .build();
  }
}
