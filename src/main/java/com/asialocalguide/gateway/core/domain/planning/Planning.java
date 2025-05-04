package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.BaseEntity;
import com.asialocalguide.gateway.core.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Planning extends BaseEntity {

  @Getter @NotBlank private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @Getter
  @NotNull
  private User user;

  @OneToMany(mappedBy = "planning", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotEmpty
  private Set<DayPlan> dayPlans = new HashSet<>();

  @CreatedDate @Getter private Instant createdDate;

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
}
