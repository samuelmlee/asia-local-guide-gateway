package com.asialocalguide.gateway.viator.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.asialocalguide.gateway.viator.dto.ViatorActivityAvailabilityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDetailDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityResponseDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivitySearchDTO;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationResponseDTO;
import com.asialocalguide.gateway.viator.exception.ViatorApiException;

/**
 * HTTP client for the Viator REST API.
 *
 * <p>Wraps the configured {@link RestClient} to call the Viator destinations,
 * product search, product detail, and availability schedule endpoints.
 * HTTP 404 responses are treated as empty results rather than errors.
 */
@Component
public class ViatorClient {

	private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";

	private final RestClient viatorRestClient;

	/**
	 * @param viatorRestClient the pre-configured Viator REST client bean
	 */
	public ViatorClient(RestClient viatorRestClient) {
		this.viatorRestClient = viatorRestClient;
	}

	/**
	 * Fetches all Viator destinations localised for the given language.
	 *
	 * @param languageIsoCode the ISO language code to pass in the {@code Accept-Language} header
	 * @return list of destinations; empty if the API returns 404
	 * @throws ViatorApiException on any non-404 client or server error
	 */
	public List<ViatorDestinationDTO> getAllDestinationsForLanguage(String languageIsoCode) {
		try {
			ResponseEntity<ViatorDestinationResponseDTO> entity = viatorRestClient.get()
					.uri("/destinations")
					.headers(httpHeaders -> httpHeaders.set(ACCEPT_LANGUAGE_HEADER, languageIsoCode))
					.retrieve()
					.onStatus(
							// Exclude 404 from errors
							status -> (status.is4xxClientError() && status != HttpStatus.NOT_FOUND)
									|| status.is5xxServerError(),
							(req, res) -> {
								if (res.getStatusCode() != HttpStatus.NOT_FOUND) {
									handleViatorError(res);
								}
							})
					.toEntity(ViatorDestinationResponseDTO.class);

			if (entity.getStatusCode() == HttpStatus.NOT_FOUND) {
				return List.of();
			}

			return Optional.ofNullable(entity.getBody())
					.map(ViatorDestinationResponseDTO::destinations)
					.orElseGet(List::of);

		} catch (RestClientException e) {
			throw new ViatorApiException("Failed to call Destination API: " + e.getMessage(), e);
		}
	}

	/**
	 * Searches for activities using the Viator product search endpoint.
	 *
	 * @param languageIsoCode the ISO language code to pass in the {@code Accept-Language} header
	 * @param searchDTO       the search criteria (destination, dates, tags, pagination)
	 * @return list of matching activities; empty if the API returns 404
	 * @throws ViatorApiException on any non-404 client or server error
	 */
	public List<ViatorActivityDTO> getActivitiesByRequestAndLanguage(String languageIsoCode,
			ViatorActivitySearchDTO searchDTO) {
		try {
			ResponseEntity<ViatorActivityResponseDTO> entity = viatorRestClient.post()
					.uri("/products/search")
					.headers(httpHeaders -> httpHeaders.set(ACCEPT_LANGUAGE_HEADER, languageIsoCode))
					.body(searchDTO)
					.retrieve()
					.onStatus(
							// Exclude 404 from errors
							status -> (status.is4xxClientError() && status != HttpStatus.NOT_FOUND)
									|| status.is5xxServerError(),
							(req, res) -> {
								if (res.getStatusCode() != HttpStatus.NOT_FOUND) {
									handleViatorError(res);
								}
							})
					.toEntity(ViatorActivityResponseDTO.class);

			if (entity.getStatusCode() == HttpStatus.NOT_FOUND) {
				return List.of();
			}

			return Optional.ofNullable(entity.getBody()).map(ViatorActivityResponseDTO::products).orElseGet(List::of);

		} catch (RestClientException e) {
			throw new ViatorApiException("Failed to call Products Search API: " + e.getMessage(), e);
		}
	}

	/**
	 * Fetches detailed information for a single activity by its product code.
	 *
	 * @param languageIsoCode the ISO language code to pass in the {@code Accept-Language} header
	 * @param activityId      the Viator product code
	 * @return the activity detail, or empty if the activity is not found (404)
	 * @throws ViatorApiException on any non-404 client or server error
	 */
	public Optional<ViatorActivityDetailDTO> getActivityByIdAndLanguage(String languageIsoCode, String activityId) {
		try {
			ResponseEntity<ViatorActivityDetailDTO> entity = viatorRestClient.get()
					.uri("/products/{activityId}", activityId)
					.headers(httpHeaders -> httpHeaders.set(ACCEPT_LANGUAGE_HEADER, languageIsoCode))
					.retrieve()
					.onStatus(
							// Exclude 404 from errors
							status -> (status.is4xxClientError() && status != HttpStatus.NOT_FOUND)
									|| status.is5xxServerError(),
							(req, res) -> {
								if (res.getStatusCode() != HttpStatus.NOT_FOUND) {
									handleViatorError(res);
								}
							})
					.toEntity(ViatorActivityDetailDTO.class);

			if (entity.getStatusCode() == HttpStatus.NOT_FOUND) {
				return Optional.empty();
			}

			return Optional.ofNullable(entity.getBody());

		} catch (RestClientException e) {

			throw new ViatorApiException("Failed to call Products by Product Code API: " + e.getMessage(), e);
		}
	}

	/**
	 * Fetches the availability schedule for the given product code.
	 *
	 * @param productCode the Viator product code
	 * @return the availability schedule, or empty if not found (404)
	 * @throws ViatorApiException on any non-404 client or server error
	 */
	public Optional<ViatorActivityAvailabilityDTO> getAvailabilityByProductCode(String productCode) {
		try {
			ResponseEntity<ViatorActivityAvailabilityDTO> entity = viatorRestClient.get()
					.uri("/availability/schedules/{productCode}", productCode)
					.retrieve()
					.onStatus(
							// Exclude 404 from errors
							status -> (status.is4xxClientError() && status != HttpStatus.NOT_FOUND)
									|| status.is5xxServerError(),
							(req, res) -> {
								if (res.getStatusCode() != HttpStatus.NOT_FOUND) {
									handleViatorError(res);
								}
							})
					.toEntity(ViatorActivityAvailabilityDTO.class);

			if (entity.getStatusCode() == HttpStatus.NOT_FOUND) {
				return Optional.empty();
			}

			return Optional.ofNullable(entity.getBody());

		} catch (RestClientException e) {
			throw new ViatorApiException("Failed to call Availability Schedules API: " + e.getMessage(), e);
		}
	}

	private void handleViatorError(ClientHttpResponse response) throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {

			String responseBody = reader.lines().collect(Collectors.joining("\n"));

			throw new ViatorApiException("Viator API error: " + response.getStatusCode() + " - " + responseBody);
		}
	}
}
