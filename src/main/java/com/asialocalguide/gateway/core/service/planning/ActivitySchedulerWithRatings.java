package com.asialocalguide.gateway.core.service.planning;

import com.asialocalguide.gateway.core.domain.planning.ActivityPlanningData;
import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import com.google.ortools.util.Domain;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
public class ActivitySchedulerWithRatings {

  private ActivitySchedulerWithRatings() {}

  /**
   * Generates an optimal activity schedule using constraint programming.
   *
   * @param activityPlanningData Contains availability matrix, ratings, and durations
   * @return 3D array [activity][day][timeSlot] indicating scheduled times
   * @throws IllegalArgumentException If input data is invalid
   * @throws IllegalStateException If OR-Tools native libraries fail to load
   */
  public static boolean[][][] scheduleActivities(ActivityPlanningData activityPlanningData) {
    Objects.requireNonNull(activityPlanningData);

    // Map activities into availability, ratings, and durations
    boolean[][][] availabilityMatrix = activityPlanningData.getAvailabilityMatrix();
    int[] activityRatings = activityPlanningData.getRatings();
    int[] activityDurations = activityPlanningData.getDurations();

    validateActivityData(availabilityMatrix, activityRatings, activityDurations);

    loadNativeLibraries();

    // Get scheduling dimensions
    int numTimeSlotsPerDay = availabilityMatrix[0][0].length;

    int numActivities = availabilityMatrix.length;
    int numDays = availabilityMatrix[0].length;
    int numTimeSlots = availabilityMatrix[0][0].length; // 24 slots per day

    CpModel model = new CpModel();
    IntVar[] startTimes = new IntVar[numActivities];
    IntVar[] endTimes = new IntVar[numActivities];
    IntervalVar[] activityIntervals = new IntervalVar[numActivities];
    BoolVar[] isAssigned = new BoolVar[numActivities];
    List<IntervalVar> allIntervals = new ArrayList<>();

    for (int a = 0; a < numActivities; a++) {
      List<Integer> validStartTimeslots = getValidStartTimeslots(numDays, numTimeSlotsPerDay, availabilityMatrix, a);

      if (validStartTimeslots.isEmpty()) {
        continue;
      }

      // Create CP-SAT variables for this activity
      startTimes[a] =
          model.newIntVarFromDomain(
              Domain.fromValues(validStartTimeslots.stream().mapToLong(i -> i).toArray()), "start_activity_" + a);
      // Add a buffer of 3 time slots to the duration between activities
      int duration = activityDurations[a] + 3;
      endTimes[a] = model.newIntVar(0, (long) numDays * numTimeSlotsPerDay, "end_activity_" + a);
      isAssigned[a] = model.newBoolVar("is_assigned_" + a);
      // Create interval variable representing the activity's time slot
      activityIntervals[a] =
          model.newOptionalIntervalVar(
              startTimes[a], model.newConstant(duration), endTimes[a], isAssigned[a], "interval_activity_" + a);

      allIntervals.add(activityIntervals[a]);
    }

    // Add no-overlap constraint for all activities
    model.addNoOverlap(allIntervals);

    // Ensure each activity is scheduled at most once
    for (int a = 0; a < numActivities; a++) {
      if (startTimes[a] != null) {
        model.addLessOrEqual(isAssigned[a], 1);
      }
    }

    // Objective: Maximize the sum of activity ratings of assigned activities
    LinearExprBuilder objective = LinearExpr.newBuilder();
    for (int a = 0; a < numActivities; a++) {
      if (startTimes[a] != null) {
        objective.addTerm(isAssigned[a], activityRatings[a]);
      }
    }
    model.maximize(objective.build());

    CpSolver solver = new CpSolver();
    CpSolverStatus status = solver.solve(model);

    return buildScheduleFromSolution(numActivities, numDays, numTimeSlots, status, startTimes, solver, isAssigned);
  }

  /** Loads required OR-Tools native libraries */
  private static void loadNativeLibraries() {
    try {
      Loader.loadNativeLibraries();
    } catch (Exception e) {
      throw new IllegalStateException("OR-Tools library loading failed", e);
    }
  }

  /**
   * Validates consistency of activity data dimensions
   *
   * @throws IllegalArgumentException If: - Any input array is empty - Array lengths don't match
   */
  private static void validateActivityData(boolean[][][] availabilityMatrix, int[] ratings, int[] durations) {
    if (ArrayUtils.isEmpty(availabilityMatrix) || ArrayUtils.isEmpty(ratings) || ArrayUtils.isEmpty(durations)) {
      throw new IllegalArgumentException("ActivityData fields must not be empty");
    }
    int numActivities = availabilityMatrix.length;
    if (ratings.length != numActivities || durations.length != numActivities) {
      throw new IllegalArgumentException("Inconsistent activity data sizes");
    }
  }

  /**
   * Converts 3D availability matrix to absolute time slots for an activity
   *
   * @param numDays Total number of scheduling days
   * @param numTimeSlotsPerDay Time slots per day (typically 24)
   * @param availabilityMatrix 3D availability matrix [activity][day][slot]
   * @param activityIndex Index of the activity to process
   * @return List of absolute time slots (day*slotsPerDay + slot)
   */
  private static List<Integer> getValidStartTimeslots(
      int numDays, int numTimeSlotsPerDay, boolean[][][] availabilityMatrix, int activityIndex) {
    List<Integer> validStartTimes = new ArrayList<>();

    // Convert 2D (day, slot) availability to 1D absolute slots
    for (int d = 0; d < numDays; d++) {
      for (int t = 0; t < numTimeSlotsPerDay; t++) {
        if (availabilityMatrix[activityIndex][d][t]) {
          // Absolute slot index is day * time slot per day + time slot assigned
          int absoluteSlot = d * numTimeSlotsPerDay + t;
          validStartTimes.add(absoluteSlot);
        }
      }
    }
    return validStartTimes;
  }

  /**
   * Converts solver solution to a 3D schedule array
   *
   * @param status Solver result status
   * @return 3D schedule array with true values for scheduled times
   */
  private static boolean[][][] buildScheduleFromSolution(
      int numActivities,
      int numDays,
      int numTimeSlots,
      CpSolverStatus status,
      IntVar[] startTimes,
      CpSolver solver,
      BoolVar[] isAssigned) {
    boolean[][][] finalSchedule = new boolean[numActivities][numDays][numTimeSlots];
    if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
      for (int a = 0; a < numActivities; a++) {
        if (startTimes[a] != null && solver.value(isAssigned[a]) == 1) {
          int scheduledTime = (int) solver.value(startTimes[a]);
          int scheduledDay = scheduledTime / numTimeSlots;
          int scheduledSlot = scheduledTime % numTimeSlots;
          finalSchedule[a][scheduledDay][scheduledSlot] = true;
        }
      }
    } else {
      log.warn("No feasible solution found. Solver status: {}", status);
    }
    return finalSchedule;
  }
}
