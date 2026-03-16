package com.asialocalguide.gateway.core.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class WebSecurityConfig {
	
	@Value("${app.cors.allowed-origins}")
	private String allowedOrigins;

	private final JwtConverterProvider jwtConverterProvider;

	public WebSecurityConfig(JwtConverterProvider jwtConverterProvider) {
		this.jwtConverterProvider = jwtConverterProvider;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// No cookies set = No CSRF
		http.csrf(AbstractHttpConfigurer::disable);

		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

		http.authorizeHttpRequests(authz -> authz
				// Public endpoints
				.requestMatchers("/v1/activity-tags/**",
						"/v1/auth/check-email",
						"/v1/destinations/autocomplete",
						"/v1/destinations/sync/**",
						"/v1/plannings/**")
				.permitAll()
	            // Allow POST /v1/users, registration without auth
	            .requestMatchers(HttpMethod.POST, "/v1/users")
	            .permitAll()
	            // Require auth for everything else under /v1/users/**
	            .requestMatchers("/v1/users/**")
	            .authenticated()
				.anyRequest()
				// TODO: Create API to add admin role
				.authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(
						jwt -> jwt.jwtAuthenticationConverter(jwtConverterProvider.getJwtAuthenticationConverter())));

		return http.build();
	}

	@Bean
	UrlBasedCorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Accept-Language"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
