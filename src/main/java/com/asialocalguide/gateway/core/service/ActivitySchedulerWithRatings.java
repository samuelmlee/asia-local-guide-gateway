package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class ActivitySchedulerWithRatings {

  public boolean[][][] scheduleActivities(
      boolean[][][] activityAvailability, List<ViatorActivityDTO> activities) {

    Loader.loadNativeLibraries();

    // Problem parameters
    final int numActivities = activityAvailability.length;
    final int numDays = activityAvailability[0].length;
    final int numTimeSlots = activityAvailability[0][0].length;

    final int[] allActivities = IntStream.range(0, numActivities).toArray();
    final int[] allDays = IntStream.range(0, numDays).toArray();
    final int[] allTimeSlots = IntStream.range(0, numTimeSlots).toArray();

    // Extract ratings from ViatorActivityDTO
    final int[] activityRatings =
        activities.stream()
            .mapToInt(
                activity ->
                    (int)
                        Math.round(
                            activity.reviews() != null
                                    && activity.reviews().combinedAverageRating() != null
                                ? activity.reviews().combinedAverageRating()
                                    * 10 // Convert to integer weight
                                : 10)) // Default low rating if missing
            .toArray();

    // Extract durations from ViatorActivityDTO
    final int[] activityDurations =
        IntStream.range(0, activities.size())
            .map(
                a -> {
                  ViatorActivityDTO activity = activities.get(a);
                  int durationHours = 1; // Default if unknown

                  if (activity.duration() != null) {
                    if (activity.duration().fixedDurationInMinutes() != null) {
                      durationHours = activity.duration().fixedDurationInMinutes() / 60;
                    } else if (activity.duration().variableDurationFromMinutes() != null) {
                      durationHours = activity.duration().variableDurationFromMinutes() / 60;
                    }
                  }

                  // Apply mapping rules
                  int mappedDuration;
                  if (durationHours < 4) {
                    mappedDuration = 1;
                  } else if (durationHours < 6) {
                    mappedDuration = 2;
                  } else {
                    mappedDuration = 3;
                  }

                  // Check if the activity starts in the evening
                  boolean startsInEvening = isActivityStartingInEvening(activityAvailability[a]);

                  // If the activity starts in the evening, force duration to 1 timeslot
                  return startsInEvening ? 1 : mappedDuration;
                })
            .toArray();

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
              activityRatings[a] * 10L + 1); // rating * 10 + 1 as an example weighting
        }
      }
    }
    LinearExpr objective = objectiveBuilder.build();
    model.maximize(objective);

    // Solver
    CpSolver solver = new CpSolver();
    CpSolverStatus status = solver.solve(model);

    printSolution(
        status, allDays, allTimeSlots, allActivities, solver, activityScheduled, activityRatings);

    // Prepare result schedule (final boolean[][][] assignment)
    boolean[][][] finalSchedule = new boolean[numActivities][numDays][numTimeSlots];

    if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
      for (int a : allActivities) {
        for (int d : allDays) {
          for (int t : allTimeSlots) {
            finalSchedule[a][d][t] = solver.booleanValue(activityScheduled[a][d][t]);
          }
        }
      }
    } else {
      System.out.println("No feasible solution found. Solver status: " + status);
    }

    return finalSchedule;
  }

  private static void printSolution(
      CpSolverStatus status,
      int[] allDays,
      int[] allTimeSlots,
      int[] allActivities,
      CpSolver solver,
      Literal[][][] activityScheduled,
      int[] activityRatings) {
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

  private boolean isActivityStartingInEvening(boolean[][] availabilityForActivity) {
    for (boolean[] dayAvailability : availabilityForActivity) {
      if (dayAvailability[2]) { // Index 2 = Evening slot
        return true;
      }
    }
    return false;
  }
}
