package com.asialocalguide.gateway.viator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Spring configuration that creates the Viator {@link RestClient} bean.
 *
 * <p>Configures the base URL, authentication header, API version, and the
 * request logging interceptor from {@link ViatorProperties}.
 */
@Configuration
public class ViatorConfig {

	private final ViatorProperties viatorProperties;

	/**
	 * @param viatorProperties externalized Viator API configuration properties
	 */
	public ViatorConfig(ViatorProperties viatorProperties) {
		this.viatorProperties = viatorProperties;
	}

	@Bean
	RestClient viatorRestClient() {

		return RestClient.builder().baseUrl(viatorProperties.baseUrl()).defaultHeaders(headers -> {
			headers.set(viatorProperties.authHeader(), viatorProperties.apiKey());
			headers.set("Accept", MediaType.APPLICATION_JSON + ";" + viatorProperties.apiVersion());
		}).requestInterceptor(new ViatorRequestInterceptor()).build();
	}
}
