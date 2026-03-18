package com.asialocalguide.gateway.firebase.service;

import com.asialocalguide.gateway.core.config.JwtConverterProvider;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

/**
 * {@link JwtConverterProvider} implementation for Firebase-issued JWTs.
 *
 * <p>Extracts the {@code roles} claim from the JWT and maps each value to a Spring Security
 * {@link SimpleGrantedAuthority} with the {@code ROLE_} prefix.
 */
@Component
public class FirebaseJwtConverterProvider implements JwtConverterProvider {

	/**
	 * Returns a {@link JwtAuthenticationConverter} that reads the {@code roles} JWT claim
	 * and maps each entry to a {@code ROLE_}-prefixed {@link SimpleGrantedAuthority}.
	 *
	 * @return a configured converter; never {@code null}
	 */
	@Override
	public JwtAuthenticationConverter getJwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

		converter.setJwtGrantedAuthoritiesConverter(jwt -> Optional.ofNullable(jwt.getClaimAsStringList("roles"))
				.orElse(List.of())
				.stream()
				.map(role -> new SimpleGrantedAuthority("ROLE_" + role))
				.collect(Collectors.toList()));

		return converter;
	}
}
