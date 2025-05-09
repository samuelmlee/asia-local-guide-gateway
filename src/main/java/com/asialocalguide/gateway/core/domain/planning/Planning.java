package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.BaseEntity;
import com.asialocalguide.gateway.core.domain.user.AppUser;
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
  @JoinColumn(name = "app_user_id")
  @Getter
  @NotNull
  private AppUser appUser;

  @OneToMany(mappedBy = "planning", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotEmpty
  private Set<DayPlan> dayPlans = new HashSet<>();

  @CreatedDate @Getter private Instant createdDate;

  public Planning(AppUser appUser, String name) {
    if (appUser == null || name == null) {
      throw new IllegalArgumentException(String.format("User: %s or name: %s cannot be null", appUser, name));
    }
    this.appUser = appUser;
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
