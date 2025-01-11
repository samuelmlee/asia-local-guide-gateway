package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.dto.ActivityPlanningDTO;
import com.asialocalguide.gateway.core.dto.ActivityPlanningRequestDTO;
import com.asialocalguide.gateway.viator.service.ViatorActivityService;
import org.springframework.stereotype.Service;

@Service
public class PlanningService {

  private final ViatorActivityService viatorActivityService;

  public PlanningService(ViatorActivityService viatorActivityService) {
    this.viatorActivityService = viatorActivityService;
  }

  public ActivityPlanningDTO generateActivityPlan(ActivityPlanningRequestDTO request) {

    return null;
  }
}
