package com.asialocalguide.gateway.core.dto.planning;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PlanningCreateRequestDTO(@NotBlank String name, @NotEmpty List<DayPlanDTO> dayPlans) {}
