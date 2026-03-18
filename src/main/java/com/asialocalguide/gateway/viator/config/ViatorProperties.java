package com.asialocalguide.gateway.viator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized configuration properties for the Viator API integration.
 *
 * @param apiKey     the Viator API key used for authentication
 * @param authHeader the HTTP header name to carry the API key
 * @param baseUrl    the base URL of the Viator REST API
 * @param apiVersion the API version string appended to the {@code Accept} header
 */
@ConfigurationProperties("viator")
public record ViatorProperties(String apiKey, String authHeader, String baseUrl, String apiVersion) {
}
