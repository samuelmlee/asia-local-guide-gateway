package com.asialocalguide.gateway.planning.domain;

import com.asialocalguide.gateway.core.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a single day within a {@link Planning}.
 *
 * <p>Owned day activities are managed with cascade-all and orphan removal.
 */
@Entity
@NoArgsConstructor
public class DayPlan extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "planning_id")
	@NotNull
	private Planning planning;

	@Getter
	@NotNull
	private LocalDate date;

	@OneToMany(mappedBy = "dayPlan", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<DayActivity> dayActivities = new HashSet<>();

	/**
	 * @param date the calendar date for this day plan; must not be {@code null}
	 * @throws IllegalArgumentException if {@code date} is {@code null}
	 */
	public DayPlan(LocalDate date) {
		if (date == null) {
			throw new IllegalArgumentException("Date and planning cannot be null");
		}
		this.date = date;
	}

	void setPlanning(Planning planning) {
		this.planning = planning;
	}

	/**
	 * Returns an unmodifiable view of all activities scheduled for this day.
	 *
	 * @return set of day activities; never {@code null}
	 */
	public Set<DayActivity> getDayActivities() {
		return Collections.unmodifiableSet(dayActivities);
	}

	/**
	 * Adds an activity to this day plan, establishing the bidirectional association.
	 * Silently ignores {@code null} inputs.
	 *
	 * @param dayActivity the activity to add
	 */
	public void addDayActivity(DayActivity dayActivity) {
		if (dayActivity == null || dayActivities == null) {
			return;
		}
		dayActivity.setDayPlan(this);
		dayActivities.add(dayActivity);
	}

	/**
	 * Removes an activity from this day plan, clearing the bidirectional association.
	 * Silently ignores {@code null} inputs.
	 *
	 * @param dayActivity the activity to remove
	 */
	public void removeDayActivity(DayActivity dayActivity) {
		if (dayActivity == null || dayActivities == null) {
			return;
		}
		dayActivity.setDayPlan(null);
		dayActivities.remove(dayActivity);
	}
}
