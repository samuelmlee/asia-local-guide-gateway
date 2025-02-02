package com.asialocalguide.gateway.core.service;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import java.util.ArrayList;
import java.util.Arrays;
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
    final int[] activityRatings = {5, 5, 4, 3, 2};

    // Duration of each activity in time slots
    final int[] activityDurations = {1, 1, 2, 1, 2};

    // Activity availability for each activity, day, and time slot.
    // Dimensions: [activity][day][timeSlot]
    //
    // Here, for example:
    //   activityAvailability[a][d][t] == true means:
    //       "Activity a is available to start on day d at time slot t".
    //
    // Adjust the true/false values as needed for your scenario.
    boolean[][][] activityAvailability = {
            { // Activity 0
                    {true,  true,  false},  // Day 0
                    {true,  false, false}   // Day 1
            },
            { // Activity 1
                    {false, false, true},   // Day 0
                    {false, true,  true}    // Day 1
            },
            { // Activity 2
                    {true,  false, false},  // Day 0
                    {false, false, true}    // Day 1
            },
            { // Activity 3
                    {false, true,  true},   // Day 0
                    {true,  true,  false}   // Day 1
            },
            { // Activity 4
                    {false, true,  false},  // Day 0
                    {false, true,  true}    // Day 1
            }
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

    // Enforce activity availability and duration
    for (int a : allActivities) {
      for (int d : allDays) {
        for (int t : allTimeSlots) {
          // If the activity cannot start at this time slot OR
          // if the duration of the activity would exceed the number of timeslots,
          // then the activity cannot be scheduled at this timeslot.
          if (!activityAvailability[a][d][t] || t + activityDurations[a] > numTimeSlots) {
            model.addEquality(activityScheduled[a][d][t], 0);
          } else {
            // If the activity *can* start here, enforce the duration using implications
            for (int k = 0; k < activityDurations[a]; k++) {
              model.addImplication(activityScheduled[a][d][t], activityScheduled[a][d][t + k]);
            }
          }
        }
      }
    }

    // Ensure only one activity per time slot per day
    for (int d : allDays) {
      for (int t : allTimeSlots) {
        List<Literal> concurrentActivities = new ArrayList<>();
        for (int a : allActivities) {
          concurrentActivities.add(activityScheduled[a][d][t]);
        }
        model.addAtMostOne(concurrentActivities);
      }
    }

    // Each Activity can be assigned only once across all days
    for (int a : allActivities) {
      List<Literal> allScheduledTimes = new ArrayList<>();
      for (int d : allDays) {
        allScheduledTimes.addAll(Arrays.asList(activityScheduled[a][d]).subList(0, numTimeSlots));
      }
      // Ensure the activity is scheduled at most once in the entire schedule
      model.addAtMostOne(allScheduledTimes);
    }

    // Objective: Maximize the sum of the (rating-based) weights of scheduled activities
    LinearExprBuilder objectiveBuilder = LinearExpr.newBuilder();
    for (int d : allDays) {
      for (int t : allTimeSlots) {
        for (int a : allActivities) {
          // Add a weighted term for each activity, prioritizing higher ratings
          objectiveBuilder.addTerm(
                  activityScheduled[a][d][t],
                  activityRatings[a] * 10 + 1); // rating * 10 + 1 as an example weighting
        }
      }
    }
    LinearExpr objective = objectiveBuilder.build();
    model.maximize(objective);

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
          boolean slotFilled = false;

          for (int a : allActivities) {
            if (Boolean.TRUE.equals(solver.booleanValue(activityScheduled[a][d][t]))) {
              System.out.printf(
                      "  Time Slot %d: Activity %d (Rating: %d)%n", t, a, activityRatings[a]);
              totalScore += activityRatings[a];
              scheduledActivitiesCount++;
              slotFilled = true;
            }
          }

          if (!slotFilled) {
            System.out.printf("  Time Slot %d: No activity assigned%n", t);
          }
        }
        System.out.println();
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
