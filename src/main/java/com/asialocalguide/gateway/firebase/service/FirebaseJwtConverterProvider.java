package com.asialocalguide.gateway.firebase.service;

import com.asialocalguide.gateway.core.config.JwtConverterProvider;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

@Component
public class FirebaseJwtConverterProvider implements JwtConverterProvider {

  @Override
  public JwtAuthenticationConverter getJwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    converter.setJwtGrantedAuthoritiesConverter(
        jwt ->
            Optional.ofNullable(jwt.getClaimAsStringList("roles")).orElse(List.of()).stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList()));

    return converter;
  }
}
