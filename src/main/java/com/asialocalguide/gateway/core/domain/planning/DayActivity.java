package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Builder
@AllArgsConstructor
public class DayActivity {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "day_plan_id")
  @NotNull
  private DayPlan dayPlan;

  @NotBlank private String providerActivityId;

  @NotNull
  @Enumerated(EnumType.STRING)
  private BookingProviderName providerName;

  @NotNull private LocalDateTime startTime;

  @NotNull private LocalDateTime endTime;

  @NotBlank private String title;

  @NotBlank private String description;

  @DecimalMin(value = "0.0", inclusive = true)
  @DecimalMax(value = "5.0", inclusive = true)
  private Double combinedAverageRating;

  @Min(value = 0)
  private Integer reviewCount;

  @Min(value = 1)
  private Integer durationMinutes;

  @DecimalMin(value = "0.0", inclusive = true)
  private Double fromPrice;

  @NotBlank private String currency;

  @Embedded @NotNull private ActivityImage mainImage;

  @NotBlank private String providerUrl;

  @NotNull private LocalDateTime lastUpdated;

  void setDayPlan(DayPlan dayPlan) {
    if (dayPlan == null) {
      return;
    }
    this.dayPlan = dayPlan;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    DayActivity that = (DayActivity) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
