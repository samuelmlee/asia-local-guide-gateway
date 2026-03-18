package com.asialocalguide.gateway.core.config;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * Strategy for supplying the {@link JwtAuthenticationConverter} used by Spring Security's
 * OAuth2 resource server to convert JWTs into {@code Authentication} tokens.
 *
 * <p>Implementations are provider-specific (e.g. Firebase) and registered as Spring beans
 * so the correct converter is injected into the security filter chain.
 */
public interface JwtConverterProvider {

	/**
	 * Returns the {@link JwtAuthenticationConverter} configured for the authentication provider.
	 *
	 * @return a non-null converter instance
	 */
	JwtAuthenticationConverter getJwtAuthenticationConverter();
}
