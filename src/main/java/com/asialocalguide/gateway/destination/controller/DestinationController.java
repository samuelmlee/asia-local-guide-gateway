package com.asialocalguide.gateway.destination.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.destination.dto.DestinationDTO;
import com.asialocalguide.gateway.destination.service.DestinationService;

import jakarta.validation.constraints.NotBlank;

/**
 * REST controller for destination-related endpoints.
 *
 * <p>Provides autocomplete suggestions for destination search and triggers
 * destination synchronisation from external booking providers.
 */
@RestController
@RequestMapping("v1/destinations")
public class DestinationController {

	private final DestinationService destinationService;

	/**
	 * @param destinationService service handling destination business logic
	 */
	public DestinationController(DestinationService destinationService) {
		this.destinationService = destinationService;
	}

	/**
	 * Returns localized destination suggestions matching the given search query.
	 *
	 * <p>The response language is resolved from the {@code Accept-Language} request header.
	 *
	 * @param query the search string; must not be blank
	 * @return list of matching destination DTOs; never {@code null}
	 */
	@GetMapping("/autocomplete")
	public List<DestinationDTO> getAutocompleteSuggestions(@RequestParam(required = true) @NotBlank String query) {
		return destinationService.getAutocompleteSuggestions(query);
	}

	/**
	 * Triggers a full destination synchronisation for the specified booking provider.
	 *
	 * @param providerName the provider whose destinations should be synchronised
	 */
	@PostMapping("/sync/{providerName}")
	public void syncDestinations(@PathVariable BookingProviderName providerName) {
		destinationService.syncDestinationsForProvider(providerName);
	}
}
