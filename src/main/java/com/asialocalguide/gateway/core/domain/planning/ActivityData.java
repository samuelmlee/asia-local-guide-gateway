package com.asialocalguide.gateway.core.domain.planning;

import lombok.Data;

@Data
public class ActivityData {

  private boolean[][][] availabilityMatrix;
  private int[] ratings;
  private int[] durations;
  private String[][][] validStartTimes;

  public ActivityData(
      boolean[][][] availability, String[][][] startTimes, int[] ratings, int[] durations) {
    this.availabilityMatrix = availability;
    this.validStartTimes = startTimes;
    this.ratings = ratings;
    this.durations = durations;
  }
}
