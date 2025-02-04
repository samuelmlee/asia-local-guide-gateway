package com.asialocalguide.gateway.core.service;

import static org.junit.jupiter.api.Assertions.*;

import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import com.google.ortools.Loader;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ActivitySchedulerWithRatingsTest {

  @BeforeAll
  static void setUp() {
    // Initialize the OR-Tools Native Library
    Loader.loadNativeLibraries();
  }

  /** Test Case 1: Basic Valid Schedule */
  @Test
  void testScheduleActivities_BasicScenario() {
    boolean[][][] availability = {
      {{true, true, false}, {true, false, false}}, // Activity 0
      {{false, false, true}, {false, true, true}}, // Activity 1
      {{true, false, false}, {false, false, true}}, // Activity 2
      {{false, true, true}, {true, true, false}}, // Activity 3
      {{false, true, false}, {false, true, true}} // Activity 4
    };

    List<ViatorActivityDTO> activities = mockViatorActivities();

    boolean[][][] schedule =
        ActivitySchedulerWithRatings.scheduleActivities(availability, activities);

    assertNotNull(schedule, "Schedule should not be null.");
    assertTrue(
        checkAtLeastOneActivityScheduled(schedule), "At least one activity should be scheduled.");
  }

  /** Test Case 2: Ensures Each Activity is Scheduled at Most Once */
  @Test
  void testScheduleActivities_NoActivityDuplicate() {
    boolean[][][] availability = {
      {{true, false, false}, {false, true, false}}, // Activity 0
      {{false, true, false}, {true, false, false}}, // Activity 1
      {{true, false, false}, {false, false, true}}, // Activity 2
      {{false, true, true}, {true, false, false}}, // Activity 3
      {{false, false, true}, {false, true, true}} // Activity 4
    };

    List<ViatorActivityDTO> activities = mockViatorActivities();

    boolean[][][] schedule =
        ActivitySchedulerWithRatings.scheduleActivities(availability, activities);

    assertTrue(
        checkUniqueActivityPerSlot(schedule), "Each activity should be scheduled at most once.");
  }

  /** Test Case 3: No Available Slots for Activities */
  @Test
  void testScheduleActivities_NoFeasibleSolution() {
    boolean[][][] availability = new boolean[5][2][3]; // No true values → No availability.

    List<ViatorActivityDTO> activities = mockViatorActivities();

    boolean[][][] schedule =
        ActivitySchedulerWithRatings.scheduleActivities(availability, activities);

    assertTrue(
        checkNoActivityScheduled(schedule),
        "No activities should be scheduled if no availability exists.");
  }

  /** Test Case 4: Ensures No Activity is Scheduled Outside Allowed Time Slots */
  @Test
  void testScheduleActivities_ObeysAvailabilityConstraints() {
    boolean[][][] availability = {
      {{true, false, false}, {false, false, false}}, // Activity 0 (only morning)
      {{false, true, false}, {false, true, false}}, // Activity 1 (only afternoon)
      {{false, false, true}, {false, false, true}}, // Activity 2 (only evening)
      {{true, true, false}, {false, true, true}}, // Activity 3
      {{false, false, false}, {false, false, false}} // Activity 4 (Never available)
    };

    List<ViatorActivityDTO> activities = mockViatorActivities();

    boolean[][][] schedule =
        ActivitySchedulerWithRatings.scheduleActivities(availability, activities);

    assertTrue(
        checkObeysAvailability(schedule, availability),
        "Activities should be scheduled only in available time slots.");
  }

  /** Test Case 5: Higher Rated Activities Should Be Prioritized */
  @Test
  void testScheduleActivities_HigherRatedActivitiesPrioritized() {
    boolean[][][] availability = {
      {{true, false, false}, {false, false, false}}, // Activity 0 (Low Rating)
      {{true, false, false}, {false, false, false}}, // Activity 1 (High Rating)
      {{false, true, false}, {false, false, false}}, // Activity 2
      {{false, true, false}, {false, false, false}}, // Activity 3
      {{false, false, true}, {false, false, false}} // Activity 4
    };

    List<ViatorActivityDTO> activities = mockViatorActivities();

    boolean[][][] schedule =
        ActivitySchedulerWithRatings.scheduleActivities(availability, activities);

    assertTrue(
        checkHigherRatingsScheduled(schedule, activities),
        "Higher-rated activities should be prioritized.");
  }

  // --------------------------- MOCK DATA HELPERS ---------------------------

  private List<ViatorActivityDTO> mockViatorActivities() {
    return List.of(
        new ViatorActivityDTO(
            "ACT1",
            "Activity 1",
            "Description 1",
            null,
            mockReviews(2.0),
            null,
            "CONFIRM",
            "ITINERARY",
            null,
            null,
            null,
            null,
            null,
            null),
        new ViatorActivityDTO(
            "ACT2",
            "Activity 2",
            "Description 2",
            null,
            mockReviews(5.0),
            null,
            "CONFIRM",
            "ITINERARY",
            null,
            null,
            null,
            null,
            null,
            null),
        new ViatorActivityDTO(
            "ACT3",
            "Activity 3",
            "Description 3",
            null,
            mockReviews(3.0),
            null,
            "CONFIRM",
            "ITINERARY",
            null,
            null,
            null,
            null,
            null,
            null),
        new ViatorActivityDTO(
            "ACT4",
            "Activity 4",
            "Description 4",
            null,
            mockReviews(4.5),
            null,
            "CONFIRM",
            "ITINERARY",
            null,
            null,
            null,
            null,
            null,
            null),
        new ViatorActivityDTO(
            "ACT5",
            "Activity 5",
            "Description 5",
            null,
            mockReviews(1.0),
            null,
            "CONFIRM",
            "ITINERARY",
            null,
            null,
            null,
            null,
            null,
            null));
  }

  private ViatorActivityDTO.ReviewsDTO mockReviews(double rating) {
    return new ViatorActivityDTO.ReviewsDTO(
        List.of(new ViatorActivityDTO.ReviewsDTO.SourceDTO("source", 100, rating)), 100, rating);
  }

  // --------------------------- ASSERTION HELPERS ---------------------------

  private boolean checkAtLeastOneActivityScheduled(boolean[][][] schedule) {
    for (boolean[][] days : schedule) {
      for (boolean[] slots : days) {
        for (boolean scheduled : slots) {
          if (scheduled) return true;
        }
      }
    }
    return false;
  }

  private boolean checkUniqueActivityPerSlot(boolean[][][] schedule) {
    for (boolean[][] days : schedule) {
      for (boolean[] slots : days) {
        int count = 0;
        for (boolean scheduled : slots) {
          if (scheduled) count++;
        }
        if (count > 1) return false;
      }
    }
    return true;
  }

  private boolean checkNoActivityScheduled(boolean[][][] schedule) {
    return !checkAtLeastOneActivityScheduled(schedule);
  }

  private boolean checkObeysAvailability(boolean[][][] schedule, boolean[][][] availability) {
    for (int a = 0; a < schedule.length; a++) {
      for (int d = 0; d < schedule[a].length; d++) {
        for (int t = 0; t < schedule[a][d].length; t++) {
          if (schedule[a][d][t] && !availability[a][d][t]) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private boolean checkHigherRatingsScheduled(
      boolean[][][] schedule, List<ViatorActivityDTO> activities) {
    return schedule[1][0][0]; // Activity 1 has the highest rating and should be scheduled first.
  }
}
