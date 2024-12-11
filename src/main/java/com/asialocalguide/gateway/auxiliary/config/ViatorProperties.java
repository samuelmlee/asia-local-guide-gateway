package com.asialocalguide.gateway.auxiliary.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("viator")
public record ViatorProperties(String apiKey, String authHeader, String baseUrl) {}
