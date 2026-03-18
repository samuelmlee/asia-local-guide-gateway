package com.asialocalguide.gateway.planning.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.asialocalguide.gateway.appuser.domain.AuthProviderName;
import com.asialocalguide.gateway.core.service.auth.AuthService;
import com.asialocalguide.gateway.planning.domain.Planning;
import com.asialocalguide.gateway.planning.dto.DayPlanDTO;
import com.asialocalguide.gateway.planning.dto.PlanningCreateRequestDTO;
import com.asialocalguide.gateway.planning.dto.PlanningCreatedDTO;
import com.asialocalguide.gateway.planning.dto.PlanningRequestDTO;
import com.asialocalguide.gateway.planning.dto.PlanningSummaryDTO;
import com.asialocalguide.gateway.planning.service.PlanningService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("v1/plannings")
public class PlanningController {

	private final PlanningService planningService;

	private final AuthService authService;

	public PlanningController(PlanningService planningService, AuthService authService) {
		this.planningService = planningService;
		this.authService = authService;
	}

	@PostMapping("/generate")
	public List<DayPlanDTO> generateDayPlans(@Valid @RequestBody PlanningRequestDTO request) {
		return planningService.generateDayPlans(request);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<PlanningCreatedDTO> savePlanning(@Valid @RequestBody PlanningCreateRequestDTO planningRequest,
			Authentication authentication) {

		AuthProviderName providerName = authService.getProviderFromAuthentication(authentication)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
						"Authentication provider not recognized"));

		String userProviderId = authentication.getName();

		Planning planningCreated = planningService.savePlanning(planningRequest, providerName, userProviderId);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(planningCreated.getId())
				.toUri();

		return ResponseEntity.created(location).body(new PlanningCreatedDTO(planningCreated.getId()));
	}

	@GetMapping
	public List<PlanningSummaryDTO> getUserPlannings(Authentication authentication) {
		AuthProviderName providerName = authService.getProviderFromAuthentication(authentication)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
						"Authentication provider not recognized"));

		String userProviderId = authentication.getName();

		return planningService.getUserPlannings(providerName, userProviderId);
	}
}
