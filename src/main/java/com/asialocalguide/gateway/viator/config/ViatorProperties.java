package com.asialocalguide.gateway.viator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("viator")
public record ViatorProperties(
    String apiKey, String authHeader, String baseUrl, String apiVersion) {}
