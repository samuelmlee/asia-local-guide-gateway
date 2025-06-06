package com.asialocalguide.gateway.core.domain.planning;

import lombok.Data;

@Data
public class ActivityPlanningData {

  private boolean[][][] availabilityMatrix;
  private int[] ratings;
  private int[] durations;
  private String[][][] validStartTimes;

  public ActivityPlanningData(
      // int array for ratings: ratings weighted in the mapper
      boolean[][][] availability, String[][][] startTimes, int[] ratings, int[] durations) {
    this.availabilityMatrix = availability;
    this.validStartTimes = startTimes;
    this.ratings = ratings;
    this.durations = durations;
  }
}
