package com.asialocalguide.gateway.planning.domain;

import lombok.Data;

/**
 * Aggregated planning data for a set of activities used by the constraint-programming scheduler.
 *
 * <p>The three arrays are indexed by activity, day, and time slot respectively.
 * {@code validStartTimes} contains the provider time string (e.g. {@code "09:00"}) for each
 * combination, which is used to construct concrete {@link java.time.LocalDateTime} values.
 */
@Data
public class ActivityPlanningData {

	private boolean[][][] availabilityMatrix;
	private int[] ratings;
	private int[] durations;
	private String[][][] validStartTimes;

	/**
	 * @param availability 3D availability matrix {@code [activity][day][timeSlot]}
	 * @param startTimes   3D array of provider time strings {@code [activity][day][timeSlot]}
	 * @param ratings      weighted integer ratings per activity
	 * @param durations    duration in time-slots per activity
	 */
	public ActivityPlanningData(
			// int array for ratings: ratings weighted in the mapper
			boolean[][][] availability, String[][][] startTimes, int[] ratings, int[] durations) {
		this.availabilityMatrix = availability;
		this.validStartTimes = startTimes;
		this.ratings = ratings;
		this.durations = durations;
	}
}
