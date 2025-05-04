package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.BaseEntity;
import com.asialocalguide.gateway.core.validation.ValidTimeOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@ValidTimeOrder
public class DayActivity extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "day_plan_id")
  @NotNull
  private DayPlan dayPlan;

  @NotNull private LocalDateTime startTime;

  @NotNull private LocalDateTime endTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "activity_id", referencedColumnName = "id")
  @NotNull
  private Activity activity;

  public DayActivity(Activity activity, LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime == null || endTime == null || activity == null) {
      throw new IllegalArgumentException("Start time, end time, and activity cannot be null");
    }
    this.startTime = startTime;
    this.endTime = endTime;
    this.activity = activity;
  }

  void setDayPlan(DayPlan dayPlan) {
    this.dayPlan = dayPlan;
  }
}
