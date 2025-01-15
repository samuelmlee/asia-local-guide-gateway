package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.dto.ActivityPlanningRequestDTO;
import com.asialocalguide.gateway.core.dto.DayPlanDTO;
import com.asialocalguide.gateway.core.service.PlanningService;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/planning")
public class PlanningController {

  private final PlanningService planningService;

  public PlanningController(PlanningService planningService) {
    this.planningService = planningService;
  }

  @PostMapping("/generate")
  public List<DayPlanDTO> generateActivityPlan(@RequestBody ActivityPlanningRequestDTO request) {
    return planningService.generateActivityPlanning(request);
  }
}
