package com.asialocalguide.gateway.core.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.dto.destination.DestinationDTO;
import com.asialocalguide.gateway.core.service.destination.DestinationService;

import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("v1/destinations")
public class DestinationController {

	private final DestinationService destinationService;

	public DestinationController(DestinationService destinationService) {
		this.destinationService = destinationService;
	}

	@GetMapping("/autocomplete")
	public List<DestinationDTO> getAutocompleteSuggestions(@RequestParam(required = true) @Size(min = 1) String query) {
		return destinationService.getAutocompleteSuggestions(query);
	}

	@PostMapping("/sync/{providerName}")
	public void syncDestinations(@PathVariable BookingProviderName providerName) {
		destinationService.syncDestinationsForProvider(providerName);
	}
}
