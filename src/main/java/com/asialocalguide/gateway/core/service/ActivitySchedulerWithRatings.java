package com.asialocalguide.gateway.core.service;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ActivitySchedulerWithRatings {
  public static void main(String[] args) {
    Loader.loadNativeLibraries();

    // Problem parameters
    final int numActivities = 5; // Number of activities
    final int numDays = 1; // Number of vacation days
    final int numTimeSlots = 3; // Three time slots per day (morning, afternoon, evening)

    final int[] allActivities = IntStream.range(0, numActivities).toArray();
    final int[] allDays = IntStream.range(0, numDays).toArray();
    final int[] allTimeSlots = IntStream.range(0, numTimeSlots).toArray();

    // Review ratings for each activity (higher = better)
    final int[] activityRatings = {5, 3, 4, 2, 5};

    // Duration of each activity in time slots (e.g., activity 0 might span 2 time slots)
    final int[] activityDurations = {
      1, 2, 3, 1, 2
    }; // Duration in number of time slots per activity

    // Availability of each activity in each time slot (e.g., true means available, false means not
    // available)
    boolean[][] activityAvailability = {
      {true, true, false}, // Activity 0 is available in morning and afternoon, but not in evening
      {false, true, true}, // Activity 1 is available in afternoon and evening, but not in morning
      {true, true, true}, // Activity 2 is available in all time slots
      {false, true, false}, // Activity 3 is available only in the afternoon
      {true, false, false} // Activity 4 is available only in the morning
    };

    // Create the model
    CpModel model = new CpModel();

    // Decision variables: Activity assignment per time slot on each day
    Literal[][][] activityScheduled = new Literal[numActivities][numDays][numTimeSlots];

    for (int a : allActivities) {
      for (int d : allDays) {
        for (int t : allTimeSlots) {
          activityScheduled[a][d][t] =
              model.newBoolVar("activity_" + a + "_day_" + d + "_time_" + t);
        }
      }
    }

    // Constraint: Each activity can only be scheduled in available time slots
    for (int a : allActivities) {
      for (int d : allDays) {
        for (int t : allTimeSlots) {
          if (!activityAvailability[a][t]) {
            // If the activity is not available in this time slot, it cannot be scheduled here
            model.addEquality(activityScheduled[a][d][t], 0); // 0 means "false"
          }
        }
      }
    }

    // Constraint: Activity must span the required number of time slots
    for (int a : allActivities) {
      for (int d : allDays) {
        for (int t = 0; t <= numTimeSlots - activityDurations[a]; t++) { // Ensure activity fits
          List<Literal> slots = new ArrayList<>();
          for (int k = 0; k < activityDurations[a]; k++) {
            if (activityAvailability[a][
                t + k]) { // Check if the activity is available in this time slot
              slots.add(activityScheduled[a][d][t + k]);
            }
          }
          if (!slots.isEmpty()) {
            model.addExactlyOne(slots); // Ensure activity spans the given slots
          }
        }
      }
    }

    // Constraint: No overlapping activities in the same time slot on the same day
    for (int d : allDays) {
      for (int t : allTimeSlots) {
        List<Literal> concurrentActivities = new ArrayList<>();
        for (int a : allActivities) {
          concurrentActivities.add(activityScheduled[a][d][t]);
        }
        model.addAtMostOne(concurrentActivities); // Ensures only one activity per slot
      }
    }

    // **Objective: Maximize total review ratings**
    LinearExprBuilder totalRating = LinearExpr.newBuilder();
    for (int a : allActivities) {
      for (int d : allDays) {
        for (int t : allTimeSlots) {
          totalRating.addTerm(activityScheduled[a][d][t], activityRatings[a]);
        }
      }
    }
    model.maximize(totalRating);

    // Solver
    CpSolver solver = new CpSolver();
    CpSolverStatus status = solver.solve(model);

    // Print solution
    if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
      System.out.println("Optimal Schedule with Maximum Ratings:");
      int totalScore = 0;
      for (int d : allDays) {
        System.out.printf("Day %d:%n", d);
        for (int t : allTimeSlots) {
          for (int a : allActivities) {
            if (solver.booleanValue(activityScheduled[a][d][t])) {
              System.out.printf(
                  "  Activity %d (Rating: %d) scheduled at time slot %d%n",
                  a, activityRatings[a], t);
              totalScore += activityRatings[a];
            }
          }
        }
      }
      System.out.println("Total Review Rating: " + totalScore);
    } else {
      System.out.println("No feasible solution found.");
    }
  }
}
