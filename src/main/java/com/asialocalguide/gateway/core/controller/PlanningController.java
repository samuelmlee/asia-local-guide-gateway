package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.domain.planning.Planning;
import com.asialocalguide.gateway.core.domain.user.AuthProviderName;
import com.asialocalguide.gateway.core.dto.planning.DayPlanDTO;
import com.asialocalguide.gateway.core.dto.planning.PlanningCreateRequestDTO;
import com.asialocalguide.gateway.core.dto.planning.PlanningRequestDTO;
import com.asialocalguide.gateway.core.dto.planning.PlanningResponseDTO;
import com.asialocalguide.gateway.core.service.auth.AuthService;
import com.asialocalguide.gateway.core.service.planning.PlanningService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("v1/planning")
public class PlanningController {

  private final PlanningService planningService;

  private final AuthService authService;

  public PlanningController(PlanningService planningService, AuthService authService) {
    this.planningService = planningService;
    this.authService = authService;
  }

  @PostMapping("/generate")
  public List<DayPlanDTO> generateActivityPlan(@Valid @RequestBody PlanningRequestDTO request) {
    return planningService.generateActivityPlanning(request);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<PlanningResponseDTO> savePlanning(
      @Valid PlanningCreateRequestDTO planningRequest, Authentication authentication) {

    AuthProviderName providerName =
        authService
            .getProviderFromAuthentication(authentication)
            .orElseThrow(() -> new SecurityException("Authentication provider not recognized"));
    String userProviderId = authentication.getName();

    Planning planningCreated = planningService.savePlanning(planningRequest, providerName, userProviderId);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(planningCreated.getId()).toUri();

    return ResponseEntity.created(location).body(new PlanningResponseDTO(planningCreated.getId()));
  }
}
