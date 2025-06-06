package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class DayPlan extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "planning_id")
  @NotNull
  private Planning planning;

  @Getter @NotNull private LocalDate date;

  @OneToMany(mappedBy = "dayPlan", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<DayActivity> dayActivities = new HashSet<>();

  public DayPlan(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Date and planning cannot be null");
    }
    this.date = date;
  }

  void setPlanning(Planning planning) {
    this.planning = planning;
  }

  public Set<DayActivity> getDayActivities() {
    return Collections.unmodifiableSet(dayActivities);
  }

  public void addDayActivity(DayActivity dayActivity) {
    if (dayActivity == null || dayActivities == null) {
      return;
    }
    dayActivity.setDayPlan(this);
    dayActivities.add(dayActivity);
  }

  public void removeDayActivity(DayActivity dayActivity) {
    if (dayActivity == null || dayActivities == null) {
      return;
    }
    dayActivity.setDayPlan(null);
    dayActivities.remove(dayActivity);
  }
}
