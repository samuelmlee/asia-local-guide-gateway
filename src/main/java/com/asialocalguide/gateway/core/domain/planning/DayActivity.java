package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.validation.ValidTimeOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@ValidTimeOrder
public class DayActivity {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "day_plan_id")
  @NotNull
  private DayPlan dayPlan;

  @NotNull private LocalDateTime startTime;

  @NotNull private LocalDateTime endTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "provider_activity_id", referencedColumnName = "provider_activity_id")
  @JoinColumn(name = "booking_provider_id", referencedColumnName = "booking_provider_id")
  @NotNull
  private Activity activity;

  public DayActivity(LocalDateTime startTime, LocalDateTime endTime, Activity activity) {
    if (startTime == null || endTime == null || activity == null) {
      throw new IllegalArgumentException("Start time, end time, and activity cannot be null");
    }
    this.startTime = startTime;
    this.endTime = endTime;
    this.activity = activity;
  }

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
