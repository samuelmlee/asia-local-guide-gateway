package com.asialocalguide.gateway.auth.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import com.asialocalguide.gateway.appuser.domain.AuthProviderName;

/**
 * Service for resolving the authentication provider from a Spring Security {@link Authentication}.
 *
 * <p>Inspects JWT claims to determine which provider issued the token.
 * Currently supports Firebase tokens identified by the {@code iss} claim.
 */
@Service
public class AuthService {

	private final String firebaseProjectId;

	/**
	 * @param firebaseProjectId the Firebase project ID used to validate the JWT issuer claim,
	 *                          injected from the {@code auth.firebase.project-id} property
	 */
	public AuthService(@Value("${auth.firebase.project-id}") String firebaseProjectId) {
		this.firebaseProjectId = firebaseProjectId;
	}

	/**
	 * Determines the {@link AuthProviderName} for the given authentication token.
	 *
	 * <p>Returns {@link AuthProviderName#FIREBASE} when the JWT's {@code iss} claim matches
	 * the configured Firebase project. Returns empty for non-JWT authentications or unrecognised issuers.
	 *
	 * @param authentication the current authentication; may be any Spring Security implementation
	 * @return an Optional containing the identified provider, or empty if the provider cannot be determined
	 */
	public Optional<AuthProviderName> getProviderFromAuthentication(Authentication authentication) {
		if (authentication instanceof JwtAuthenticationToken jwtAuth) {
			Jwt jwt = jwtAuth.getToken();
			String issuer = jwt.getClaimAsString("iss");

			if (issuer != null && issuer.contains("securetoken.google.com/" + firebaseProjectId)) {
				return Optional.of(AuthProviderName.FIREBASE);
			}
		}

		return Optional.empty();
	}
}
