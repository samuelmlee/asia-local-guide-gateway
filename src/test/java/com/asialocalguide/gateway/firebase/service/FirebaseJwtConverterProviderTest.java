package com.asialocalguide.gateway.firebase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@ExtendWith(MockitoExtension.class)
class FirebaseJwtConverterProviderTest {

  @InjectMocks private FirebaseJwtConverterProvider provider;

  @Test
  void convertsSingleRoleToAuthority() {
    Jwt jwt = mock(Jwt.class);
    when(jwt.getClaimAsStringList("roles")).thenReturn(List.of("USER"));

    JwtAuthenticationConverter converter = provider.getJwtAuthenticationConverter();
    AbstractAuthenticationToken authentication = converter.convert(jwt);
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    assertEquals(1, authorities.size());
    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
  }

  @Test
  void convertsMultipleRolesToAuthorities() {
    Jwt jwt = mock(Jwt.class);
    when(jwt.getClaimAsStringList("roles")).thenReturn(List.of("USER", "ADMIN"));

    JwtAuthenticationConverter converter = provider.getJwtAuthenticationConverter();
    AbstractAuthenticationToken authentication = converter.convert(jwt);
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    assertEquals(2, authorities.size());
    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
  }

  @Test
  void returnsEmptyAuthoritiesWhenRolesListIsEmpty() {
    Jwt jwt = mock(Jwt.class);
    when(jwt.getClaimAsStringList("roles")).thenReturn(List.of());

    JwtAuthenticationConverter converter = provider.getJwtAuthenticationConverter();
    AbstractAuthenticationToken authentication = converter.convert(jwt);
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    assertTrue(authorities.isEmpty());
  }

  @Test
  void handlesNullRolesClaimGracefully() {
    Jwt jwt = mock(Jwt.class);
    when(jwt.getClaimAsStringList("roles")).thenReturn(null);

    JwtAuthenticationConverter converter = provider.getJwtAuthenticationConverter();
    AbstractAuthenticationToken authentication = converter.convert(jwt);
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    assertTrue(authorities.isEmpty());
  }
}
