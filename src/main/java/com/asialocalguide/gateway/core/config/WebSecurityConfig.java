package com.asialocalguide.gateway.core.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class WebSecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // No cookies set = No CSRF
    http.csrf(AbstractHttpConfigurer::disable);

    http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

    http.authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers(
                        "/v1/activity-tags/**",
                        "/v1/auth/check-email",
                        "/v1/destinations/autocomplete",
                        "/v1/planning/**",
                        "/v1/users/**")
                    .permitAll()
                    .anyRequest()
                    // TODO: Create API to add admin role
                    .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

    return http.build();
  }

  @Bean
  UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:4200"));
    configuration.setAllowedMethods(List.of("*"));
    configuration.setAllowedHeaders(List.of("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
