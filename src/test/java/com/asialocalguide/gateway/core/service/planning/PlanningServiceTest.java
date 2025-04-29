package com.asialocalguide.gateway.core.service.planning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.ActivityPlanningData;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningData;
import com.asialocalguide.gateway.core.dto.planning.DayActivityDTO;
import com.asialocalguide.gateway.core.dto.planning.DayPlanDTO;
import com.asialocalguide.gateway.core.dto.planning.PlanningRequestDTO;
import com.asialocalguide.gateway.core.repository.PlanningRepository;
import com.asialocalguide.gateway.core.service.strategy.FetchPlanningDataStrategy;
import com.asialocalguide.gateway.core.service.user.UserService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanningServiceTest {

  @Mock private FetchPlanningDataStrategy planningStrategy1;

  @Mock private FetchPlanningDataStrategy planningStrategy2;

  @Mock private UserService userService;

  @Mock private ActivityService activityService;

  @Mock private PlanningRepository planningRepository;

  private PlanningService planningService;

  private PlanningRequestDTO validRequest;
  private final LocalDate today = LocalDate.now();
  private final LocalDate tomorrow = today.plusDays(1);

  @BeforeEach
  void setup() {
    planningService =
        new PlanningService(
            List.of(planningStrategy1, planningStrategy2), userService, activityService, planningRepository);

    validRequest = new PlanningRequestDTO(today, tomorrow, 1L, List.of("adventure"));
  }

  @Test
  void generateActivityPlanning_shouldHandleNoProviders() {
    PlanningService service = new PlanningService(List.of(), userService, activityService, planningRepository);

    List<DayPlanDTO> result = service.generateActivityPlanning(validRequest);

    assertTrue(result.isEmpty());
  }

  @Test
  void generateActivityPlanning_shouldHandleFailedProviders() {
    // Setup 2-day request
    LocalDate endDate = today.plusDays(1);
    PlanningRequestDTO request = new PlanningRequestDTO(today, endDate, 1L, List.of("adventure"));

    when(planningStrategy1.fetchProviderPlanningData(any(), any())).thenThrow(new RuntimeException("Provider error"));
    when(planningStrategy2.fetchProviderPlanningData(any(), any())).thenReturn(createTestProviderData());

    List<DayPlanDTO> result = planningService.generateActivityPlanning(request);

    // Verify exactly one activity is scheduled across all days
    int totalActivities = result.stream().mapToInt(day -> day.activities().size()).sum();
    assertEquals(1, totalActivities);
  }

  @Test
  void generateActivityPlanning_shouldHandleEmptyActivityData() {
    when(planningStrategy1.fetchProviderPlanningData(any(), any()))
        .thenReturn(
            new ProviderPlanningData(
                List.of(),
                new ActivityPlanningData(
                    new boolean[1][2][24],
                    new String[1][2][24],
                    new int[] {0}, // Use a single element array instead of empty
                    new int[] {0} // Use a single element array instead of empty
                    ),
                today));

    List<DayPlanDTO> result = planningService.generateActivityPlanning(validRequest);

    assertTrue(result.stream().allMatch(day -> day.activities().isEmpty()));
  }

  @Test
  void generateActivityPlanning_shouldHandleSchedulingFailure() {
    boolean[][][] availability = new boolean[1][2][24];

    String[][][] startTimes = new String[1][2][24];

    // Setup invalid activity data that can't be scheduled
    ActivityPlanningData invalidData =
        new ActivityPlanningData(
            availability, // Availability
            startTimes, // Start times
            new int[] {1}, // Ratings
            new int[] {8} // 8-hour duration
            );

    when(planningStrategy1.fetchProviderPlanningData(any(), any()))
        .thenReturn(new ProviderPlanningData(List.of(createTestActivity(4.5)), invalidData, today));

    List<DayPlanDTO> result = planningService.generateActivityPlanning(validRequest);

    assertTrue(result.getFirst().activities().isEmpty());
  }

  @Test
  void generateActivityPlanning_shouldCreateMultiDaySchedule() {
    // 24 slots/day format: 0=00:00-01:00, 1=01:00-02:00,...23=23:00-00:00
    int numDays = 3;
    int slotsPerDay = 24;

    // 1 activity available at 09:00, 14:00, 18:00 each day (slots 9, 14, 18)
    boolean[][][] availability = new boolean[1][numDays][slotsPerDay];
    String[][][] startTimes = new String[1][numDays][slotsPerDay];

    // Initialize availability and start times
    for (int day = 0; day < numDays; day++) {
      availability[0][day][9] = true; // 09:00-10:00
      availability[0][day][14] = true; // 14:00-15:00
      availability[0][day][18] = true; // 18:00-19:00

      Arrays.fill(startTimes[0][day], ""); // Initialize empty
      startTimes[0][day][9] = "09:00";
      startTimes[0][day][14] = "14:00";
      startTimes[0][day][18] = "18:00";
    }

    ActivityPlanningData testData =
        new ActivityPlanningData(
            availability,
            startTimes,
            new int[] {5}, // Rating
            new int[] {60});

    when(planningStrategy1.fetchProviderPlanningData(any(), any()))
        .thenReturn(
            new ProviderPlanningData(
                List.of(createTestActivity(4.5)), // Single activity instance
                testData,
                today));

    PlanningRequestDTO multiDayRequest =
        new PlanningRequestDTO(
            today,
            today.plusDays(2), // 3-day duration
            1L,
            List.of("adventure"));

    List<DayPlanDTO> result = planningService.generateActivityPlanning(multiDayRequest);

    // Verify the activity is scheduled once across all days
    int totalScheduled = result.stream().mapToInt(day -> day.activities().size()).sum();
    assertEquals(1, totalScheduled);

    // Verify it's scheduled in the optimal slot (earliest high-rated)
    DayActivityDTO scheduled = result.stream().flatMap(day -> day.activities().stream()).findFirst().orElseThrow();
    assertEquals("09:00", scheduled.startTime().format(DateTimeFormatter.ofPattern("HH:mm")));
  }

  @Test
  void generateActivityPlanning_shouldHandleTimeSlotConflicts() {
    // Setup 1-day request with 24 slots
    LocalDate endDate = today.plusDays(0); // Same day (1-day duration)
    int numDays = 1;
    int slotsPerDay = 24;

    // Activity 0: High rating (5), duration 57m (1 slot) at slot 9
    // Activity 1: Low rating (4), duration 57m (1 slot) at slot 9 (CONFLICT)
    boolean[][][] availability = {
      { // Activity 0
        new boolean[slotsPerDay]
      },
      { // Activity 1
        new boolean[slotsPerDay]
      }
    };

    String[][][] startTimes = new String[2][numDays][slotsPerDay];

    // Create conflict at slot 9
    availability[0][0][9] = true; // Activity 0 available
    availability[1][0][9] = true; // Activity 1 available (same slot)

    startTimes[0][0][9] = "09:00";
    startTimes[1][0][9] = "09:00";

    ActivityPlanningData conflictData =
        new ActivityPlanningData(
            availability,
            startTimes,
            new int[] {4, 5}, // Ratings
            new int[] {1, 1} // Durations
            );

    when(planningStrategy1.fetchProviderPlanningData(any(), any()))
        .thenReturn(
            new ProviderPlanningData(List.of(createTestActivity(4.5), createTestActivity(5)), conflictData, today));

    PlanningRequestDTO request = new PlanningRequestDTO(today, endDate, 1L, List.of("adventure"));

    List<DayPlanDTO> result = planningService.generateActivityPlanning(request);

    // Verify only 1 activity is scheduled
    assertEquals(1, result.size());
    assertEquals(1, result.getFirst().activities().size());
    assertEquals(5, result.getFirst().activities().getFirst().combinedAverageRating());
  }

  private ProviderPlanningData createTestProviderData() {
    // For 2-day request
    boolean[][][] availability = new boolean[1][2][24]; // 1 activity x 2 days x 1 slot
    availability[0][0][8] = true;
    availability[0][1][13] = true;

    String[][][] startTimes = new String[1][2][24]; // Match days
    startTimes[0][0][8] = "09:00";
    startTimes[0][1][13] = "14:00";

    return new ProviderPlanningData(
        List.of(createTestActivity(4.5)),
        new ActivityPlanningData(availability, startTimes, new int[] {5}, new int[] {1}),
        today);
  }

  private CommonActivity createTestActivity(double rating) {
    return new CommonActivity(
        "Test Activity",
        "Test Description",
        List.of(),
        new CommonActivity.CommonReviews(rating, 100),
        new CommonActivity.CommonDuration(60, 60),
        new CommonActivity.CommonPricing(50.0, "EUR"),
        "http://viator.com",
        List.of("adventure"),
        BookingProviderName.VIATOR,
        "VIATOR-123");
  }
}
