package com.asialocalguide.gateway.viator.config;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * {@link ClientHttpRequestInterceptor} that logs the HTTP method and URI of every
 * outbound Viator API request at INFO level.
 */
@Slf4j
class ViatorRequestInterceptor implements ClientHttpRequestInterceptor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {

		log.info("Viator Request - Method: {}, URI: {}", request.getMethod(), request.getURI());

		return execution.execute(request, body);
	}
}
