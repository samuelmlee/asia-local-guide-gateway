package com.asialocalguide.gateway.auxiliary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WebConfig {

  private final ViatorProperties viatorProperties;

  public WebConfig(ViatorProperties viatorProperties) {
    this.viatorProperties = viatorProperties;
  }

  @Bean
  public RestClient auxiliaryRestClient() {

    return RestClient.builder()
        .baseUrl(viatorProperties.baseUrl())
        .defaultHeader(viatorProperties.authHeader(), viatorProperties.apiKey())
        .build();
  }
}
