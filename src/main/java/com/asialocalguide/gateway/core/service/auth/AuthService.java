package com.asialocalguide.gateway.core.service.auth;

import com.asialocalguide.gateway.core.domain.user.AuthProviderName;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  public Optional<AuthProviderName> getProviderFromAuthentication(Authentication authentication) {
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = jwtAuth.getToken();
      String issuer = jwt.getClaimAsString("iss");

      if (issuer != null && issuer.contains("securetoken.google.com/asia-local-guide-webapp")) {
        return Optional.of(AuthProviderName.FIREBASE);
      }
    }

    return Optional.empty();
  }
}
