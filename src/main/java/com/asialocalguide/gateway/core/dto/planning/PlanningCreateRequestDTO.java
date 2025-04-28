package com.asialocalguide.gateway.core.dto.planning;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PlanningCreateRequestDTO(@NotBlank String name, @NotEmpty List<CreateDayPlanDTO> dayPlans) {

  public record CreateDayPlanDTO(
      @NotNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate date,
      List<CreateDayActivityDTO> activities) {}

  public record CreateDayActivityDTO(
      @NotBlank String productCode,
      @NotNull BookingProviderName bookingProviderName,
      @NotNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startTime,
      @NotNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endTime) {}
}
