package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Planning {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  private Long id;

  @Getter @NotBlank private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @Getter
  @NotNull
  private User user;

  @OneToMany(mappedBy = "planning", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotEmpty
  private Set<DayPlan> dayPlans = new HashSet<>();

  public Planning(User user, String name) {
    if (user == null || name == null) {
      throw new IllegalArgumentException("User or Name cannot be null");
    }
    this.user = user;
    this.name = name;
  }

  public Set<DayPlan> getDayPlans() {
    return Collections.unmodifiableSet(dayPlans);
  }

  public void addDayPlan(DayPlan dayPlan) {
    if (dayPlan == null || dayPlans == null) {
      return;
    }
    dayPlan.setPlanning(this);
    dayPlans.add(dayPlan);
  }

  public void removeDayPlan(DayPlan dayPlan) {
    if (dayPlan == null || dayPlans == null) {
      return;
    }
    dayPlan.setPlanning(null);
    dayPlans.remove(dayPlan);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Planning planning = (Planning) o;
    return Objects.equals(id, planning.id);
  }

  @Override
  public int hashCode() {
    return id != null ? Objects.hash(id) : super.hashCode();
  }
}
