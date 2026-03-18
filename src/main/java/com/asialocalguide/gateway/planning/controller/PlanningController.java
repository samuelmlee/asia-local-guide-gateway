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
import com.asialocalguide.gateway.auth.service.AuthService;
import com.asialocalguide.gateway.planning.domain.Planning;
import com.asialocalguide.gateway.planning.dto.DayPlanDTO;
import com.asialocalguide.gateway.planning.dto.PlanningCreateRequestDTO;
import com.asialocalguide.gateway.planning.dto.PlanningCreatedDTO;
import com.asialocalguide.gateway.planning.dto.PlanningRequestDTO;
import com.asialocalguide.gateway.planning.dto.PlanningSummaryDTO;
import com.asialocalguide.gateway.planning.service.PlanningService;

import jakarta.validation.Valid;

/**
 * REST controller for planning operations.
 *
 * <p>Exposes endpoints to generate a suggested day-by-day activity schedule,
 * persist a confirmed planning, and retrieve all plannings for the authenticated user.
 */
@RestController
@RequestMapping("v1/plannings")
public class PlanningController {

	private final PlanningService planningService;

	private final AuthService authService;

	/**
	 * @param planningService service handling planning generation and persistence
	 * @param authService     service for resolving the auth provider from a JWT
	 */
	public PlanningController(PlanningService planningService, AuthService authService) {
		this.planningService = planningService;
		this.authService = authService;
	}

	/**
	 * Generates a suggested day-plan schedule for the given request parameters.
	 *
	 * @param request the planning parameters (dates, destination, activity tags); must be valid
	 * @return ordered list of day plans, each containing scheduled activities
	 */
	@PostMapping("/generate")
	public List<DayPlanDTO> generateDayPlans(@Valid @RequestBody PlanningRequestDTO request) {
		return planningService.generateDayPlans(request);
	}

	/**
	 * Persists a confirmed planning for the authenticated user.
	 *
	 * <p>Returns HTTP 201 with a {@code Location} header pointing to the created resource,
	 * and a body containing the new planning's ID.
	 *
	 * @param planningRequest the planning to save; must be valid
	 * @param authentication  the current JWT authentication
	 * @return 201 response with the created planning's ID
	 */
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

	/**
	 * Returns all plannings belonging to the authenticated user.
	 *
	 * @param authentication the current JWT authentication
	 * @return list of planning summaries for the current user; never {@code null}
	 */
	@GetMapping
	public List<PlanningSummaryDTO> getUserPlannings(Authentication authentication) {
		AuthProviderName providerName = authService.getProviderFromAuthentication(authentication)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
						"Authentication provider not recognized"));

		String userProviderId = authentication.getName();

		return planningService.getUserPlannings(providerName, userProviderId);
	}
}
