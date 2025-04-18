package com.asialocalguide.gateway.core.config;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

public interface JwtConverterProvider {
  JwtAuthenticationConverter getJwtAuthenticationConverter();
}
