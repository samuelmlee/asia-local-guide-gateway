package com.asialocalguide.gateway.core.service;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import com.google.ortools.util.Domain;
import java.util.*;

public class ActivitySchedulerOptimized {
  public static void main(String[] args) {
    Loader.loadNativeLibraries();

    int numActivities = 50; // Increased number of activities
    int numDays = 5; // Increased to 5-day schedule
    int numTimeSlotsPerDay = 18; // 1-hour slots from 6 AM to 12 AM

    Random random = new Random();
    int[] activityRatings = new int[numActivities];
    int[] activityDurations = new int[numActivities];
    boolean[][][] availabilityMatrix = new boolean[numActivities][numDays][numTimeSlotsPerDay];

    // Generating random ratings, durations, and availability
    for (int a = 0; a < numActivities; a++) {
      activityRatings[a] = random.nextInt(10) + 1; // Ratings from 1-10
      activityDurations[a] = random.nextInt(3) + 1; // Durations between 1-3 slots

      for (int d = 0; d < numDays; d++) {
        for (int t = 0; t < numTimeSlotsPerDay; t++) {
          availabilityMatrix[a][d][t] = random.nextBoolean(); // Random availability
        }
      }
    }

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
      int duration = activityDurations[a];
      endTimes[a] = model.newIntVar(0, numDays * numTimeSlotsPerDay, "end_activity_" + a);
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

    if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
      System.out.println("Scheduled Activities:");
      for (int a = 0; a < numActivities; a++) {
        if (startTimes[a] != null && solver.value(isAssigned[a]) == 1) {
          int scheduledTime = (int) solver.value(startTimes[a]);
          int scheduledDay = scheduledTime / numTimeSlotsPerDay;
          int scheduledSlot = scheduledTime % numTimeSlotsPerDay;

          System.out.printf(
              "Activity %d scheduled on Day %d at Time Slot %d (%s) (Rating: %d)%n",
              a, scheduledDay, scheduledSlot, getTimeSlotLabel(scheduledSlot), activityRatings[a]);
        }
      }
      System.out.println("Solving in time: " + solver.wallTime());
    } else {
      System.out.println("No feasible schedule found within time limit.");
    }
  }

  // Helper method to map slot numbers to actual times (for readability)
  private static String getTimeSlotLabel(int slot) {
    String[] timeSlots = {
      "6AM-7AM", "7AM-8AM", "8AM-9AM", "9AM-10AM", "10AM-11AM", "11AM-12PM",
      "12PM-1PM", "1PM-2PM", "2PM-3PM", "3PM-4PM", "4PM-5PM", "5PM-6PM",
      "6PM-7PM", "7PM-8PM", "8PM-9PM", "9PM-10PM", "10PM-11PM", "11PM-12AM"
    };
    return timeSlots[slot];
  }
}
