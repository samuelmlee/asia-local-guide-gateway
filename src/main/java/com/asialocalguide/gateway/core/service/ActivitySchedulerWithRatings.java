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
    final int numActivities = 5;
    final int numDays = 2;
    final int numTimeSlots = 3;

    final int[] allActivities = IntStream.range(0, numActivities).toArray();
    final int[] allDays = IntStream.range(0, numDays).toArray();
    final int[] allTimeSlots = IntStream.range(0, numTimeSlots).toArray();

    // Review ratings for each activity
    final int[] activityRatings = {5, 3, 4, 2, 5};

    // Duration of each activity in time slots
    final int[] activityDurations = {1, 2, 3, 1, 2};

    // Activity availability per time slot
    boolean[][] activityAvailability = {
      {true, true, false}, // Activity 0: Morning, Afternoon
      {false, true, false}, // Activity 1: Afternoon only
      {true, false, false}, // Activity 2: Morning only
      {false, true, true}, // Activity 3: Afternoon, Evening
      {true, true, false} // Activity 4: Morning, Afternoon
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

    // Binary decision variable: Whether an activity is scheduled at all
    BoolVar[] isScheduled = new BoolVar[numActivities];
    for (int a : allActivities) {
      isScheduled[a] = model.newBoolVar("isScheduled_" + a);
    }

    // Constraint: Each activity can be assigned only once across all days
    for (int a : allActivities) {
      List<Literal> allScheduledTimes = new ArrayList<>();

      for (int d : allDays) {
        for (int t : allTimeSlots) {
          allScheduledTimes.add(activityScheduled[a][d][t]);
        }
      }

      // Ensure the activity is scheduled at most once in the entire schedule
      model.addAtMostOne(allScheduledTimes);
    }

    // **Activity Scheduling Constraint (Span Multiple Slots If Started)**
    for (int a : allActivities) {
      for (int d : allDays) {
        List<Literal> validStartTimes = new ArrayList<>();

        for (int t = 0; t <= numTimeSlots - activityDurations[a]; t++) {
          if (activityAvailability[a][t]) {
            validStartTimes.add(activityScheduled[a][d][t]);

            // If activity starts here, it must span its required duration
            for (int k = 0; k < activityDurations[a]; k++) {
              if (t + k < numTimeSlots) {
                model.addImplication(activityScheduled[a][d][t], activityScheduled[a][d][t + k]);
              }
            }
          }
        }

        // Ensure the activity is scheduled at most once in a valid start time
        model.addAtMostOne(validStartTimes);

        // If an activity is scheduled at least once, mark it as scheduled
        if (!validStartTimes.isEmpty()) {
          model.addEquality(
              LinearExpr.sum(validStartTimes.toArray(new Literal[0])), isScheduled[a]);
        }
      }
    }

    // **No Overlapping Activities in the Same Time Slot on the Same Day**
    for (int d : allDays) {
      for (int t : allTimeSlots) {
        List<Literal> concurrentActivities = new ArrayList<>();
        for (int a : allActivities) {
          concurrentActivities.add(activityScheduled[a][d][t]);
        }
        model.addAtMostOne(concurrentActivities);
      }
    }

    // **Objective: Maximize the Sum of Average Ratings for Unique Activities**
    LinearExprBuilder totalRating = LinearExpr.newBuilder();
    LinearExprBuilder totalScheduledActivities = LinearExpr.newBuilder();

    for (int a : allActivities) {
      totalScheduledActivities.addTerm(isScheduled[a], 1);
      for (int d : allDays) {
        for (int t : allTimeSlots) {
          totalRating.addTerm(activityScheduled[a][d][t], activityRatings[a]);
        }
      }
    }

    // Lambda controls the trade-off between total rating and number of unique scheduled activities
    double lambda = 0.8; // Adjust this to prioritize high ratings vs. scheduling more activities

    LinearExprBuilder totalWeightedObjective = LinearExpr.newBuilder();
    totalWeightedObjective.addTerm(totalRating.build(), (int) (lambda * 100));
    totalWeightedObjective.addTerm(totalScheduledActivities.build(), (int) ((1 - lambda) * 100));

    // Maximize weighted objective
    model.maximize(totalWeightedObjective.build());

    // Solver
    CpSolver solver = new CpSolver();
    CpSolverStatus status = solver.solve(model);

    // Print solution
    if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
      System.out.println("Optimal Schedule with Maximum Average Ratings:");
      int totalScore = 0;
      int scheduledActivitiesCount = 0;

      for (int d : allDays) {
        System.out.printf("Day %d:%n", d);
        for (int t : allTimeSlots) {
          for (int a : allActivities) {
            if (solver.booleanValue(activityScheduled[a][d][t])) {
              System.out.printf(
                  "  Activity %d (Rating: %d) scheduled at time slot %d%n",
                  a, activityRatings[a], t);
              totalScore += activityRatings[a];
              scheduledActivitiesCount++;
            }
          }
        }
      }

      double avgRating =
          scheduledActivitiesCount > 0 ? (double) totalScore / scheduledActivitiesCount : 0;
      System.out.println("Total Review Rating: " + totalScore);
      System.out.println("Average Rating per Scheduled Activity: " + avgRating);
    } else {
      System.out.println("No feasible solution found. Solver status: " + status);
    }
  }
}
