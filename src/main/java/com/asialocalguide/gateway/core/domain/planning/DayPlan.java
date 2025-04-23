package com.asialocalguide.gateway.core.domain.planning;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class DayPlan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  private Long id;

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

  void setDate(LocalDate date) {
    this.date = date;
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

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    DayPlan dayPlan = (DayPlan) o;
    return Objects.equals(id, dayPlan.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
