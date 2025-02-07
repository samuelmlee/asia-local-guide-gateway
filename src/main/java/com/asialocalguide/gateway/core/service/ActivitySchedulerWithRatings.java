package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.ActivityData;
import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import com.google.ortools.util.Domain;
import java.util.ArrayList;
import java.util.List;

public class ActivitySchedulerWithRatings {

  private ActivitySchedulerWithRatings() {}

  public static boolean[][][] scheduleActivities(ActivityData activityData) {

    Loader.loadNativeLibraries();

    // Map activities into availability, ratings, and durations
    boolean[][][] availabilityMatrix = activityData.getAvailabilityMatrix();
    int[] activityRatings = activityData.getRatings();
    int[] activityDurations = activityData.getDurations();

    int numTimeSlotsPerDay = availabilityMatrix[0][0].length;

    int numActivities = availabilityMatrix.length;
    int numDays = availabilityMatrix[0].length;
    int numTimeSlots = availabilityMatrix[0][0].length; // 23 slots per day

    CpModel model = new CpModel();
    IntVar[] startTimes = new IntVar[numActivities];
    IntVar[] endTimes = new IntVar[numActivities];
    IntervalVar[] activityIntervals = new IntervalVar[numActivities];
    BoolVar[] isAssigned = new BoolVar[numActivities];
    List<IntervalVar> allIntervals = new ArrayList<>();

    for (int a = 0; a < numActivities; a++) {
      List<Integer> validStartTimes = new ArrayList<>();
      for (int d = 0; d < numDays; d++) {
        for (int t = 0; t < numTimeSlotsPerDay; t++) {
          if (availabilityMatrix[a][d][t]) {
            int absoluteSlot = d * numTimeSlotsPerDay + t;
            validStartTimes.add(absoluteSlot);
          }
        }
      }

      if (validStartTimes.isEmpty()) continue;

      startTimes[a] =
          model.newIntVarFromDomain(
              Domain.fromValues(validStartTimes.stream().mapToLong(i -> i).toArray()),
              "start_activity_" + a);
      // Add a buffer of 3 time slots to the duration between activities
      int duration = activityDurations[a] + 3;
      endTimes[a] = model.newIntVar(0, (long) numDays * numTimeSlotsPerDay, "end_activity_" + a);
      isAssigned[a] = model.newBoolVar("is_assigned_" + a);
      activityIntervals[a] =
          model.newOptionalIntervalVar(
              startTimes[a],
              model.newConstant(duration),
              endTimes[a],
              isAssigned[a],
              "interval_activity_" + a);

      allIntervals.add(activityIntervals[a]);
    }

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
      System.out.println("No feasible solution found. Solver status: " + status);
    }
    return finalSchedule;
  }
}
