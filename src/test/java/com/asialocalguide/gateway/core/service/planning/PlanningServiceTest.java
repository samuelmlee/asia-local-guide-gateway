package com.asialocalguide.gateway.core.service.planning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.*;
import com.asialocalguide.gateway.core.domain.user.AppUser;
import com.asialocalguide.gateway.core.domain.user.AuthProviderName;
import com.asialocalguide.gateway.core.dto.planning.DayActivityDTO;
import com.asialocalguide.gateway.core.dto.planning.DayPlanDTO;
import com.asialocalguide.gateway.core.dto.planning.PlanningCreateRequestDTO;
import com.asialocalguide.gateway.core.dto.planning.PlanningRequestDTO;
import com.asialocalguide.gateway.core.exception.PlanningCreationException;
import com.asialocalguide.gateway.core.exception.UserNotFoundException;
import com.asialocalguide.gateway.core.repository.PlanningRepository;
import com.asialocalguide.gateway.core.service.appuser.AppUserService;
import com.asialocalguide.gateway.core.service.strategy.FetchPlanningDataStrategy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanningServiceTest {

  @Mock private FetchPlanningDataStrategy planningStrategy1;

  @Mock private FetchPlanningDataStrategy planningStrategy2;

  @Mock private AppUserService appUserService;

  @Mock private ActivityService activityService;

  @Mock private PlanningRepository planningRepository;

  private PlanningService planningService;

  @Captor ArgumentCaptor<Planning> planningCaptor;

  private PlanningRequestDTO validRequest;
  private final LocalDate today = LocalDate.now();
  private final LocalDate tomorrow = today.plusDays(1);

  private AppUser testAppUser;
  private Activity testActivity;
  private PlanningCreateRequestDTO validCreateRequest;
  private AuthProviderName authProviderName;
  private String userProviderId;
  private PlanningCreateRequestDTO.CreateDayActivityDTO createDayActivityDTO;
  private PlanningCreateRequestDTO.CreateDayPlanDTO createDayPlanDTO;

  @BeforeEach
  void setup() {
    planningService =
        new PlanningService(
            List.of(planningStrategy1, planningStrategy2), appUserService, activityService, planningRepository);

    validRequest = new PlanningRequestDTO(today, tomorrow, UUID.randomUUID(), List.of("adventure"));

    testAppUser = createTestUser();
    testActivity = createTestActivity();
    userProviderId = "user123";
    authProviderName = AuthProviderName.FIREBASE;

    LocalDateTime startTime = LocalDateTime.now().plusHours(1);
    LocalDateTime endTime = startTime.plusHours(2);
    createDayActivityDTO = createTestDayActivityDTO(startTime, endTime);
    createDayPlanDTO = createTestDayPlanDTO(today, List.of(createDayActivityDTO));
    validCreateRequest = createTestPlanningCreateRequestDTO(List.of(createDayPlanDTO));
  }

  @Test
  void generateActivityPlanning_shouldHandleNoProviders() {
    PlanningService service = new PlanningService(List.of(), appUserService, activityService, planningRepository);

    List<DayPlanDTO> result = service.generateActivityPlanning(validRequest);

    assertTrue(result.isEmpty());
  }

  @Test
  void generateActivityPlanning_shouldHandleFailedProviders() {
    // Setup 2-day request
    LocalDate endDate = today.plusDays(1);
    PlanningRequestDTO request = new PlanningRequestDTO(today, endDate, UUID.randomUUID(), List.of("adventure"));

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
        .thenReturn(new ProviderPlanningData(List.of(createTestCommonActivity(4.5)), invalidData, today));

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
                List.of(createTestCommonActivity(4.5)), // Single activity instance
                testData,
                today));

    PlanningRequestDTO multiDayRequest =
        new PlanningRequestDTO(
            today,
            today.plusDays(2), // 3-day duration
            UUID.randomUUID(),
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
            new ProviderPlanningData(
                List.of(createTestCommonActivity(4.5), createTestCommonActivity(5)), conflictData, today));

    PlanningRequestDTO request = new PlanningRequestDTO(today, endDate, UUID.randomUUID(), List.of("adventure"));

    List<DayPlanDTO> result = planningService.generateActivityPlanning(request);

    // Verify only 1 activity is scheduled
    assertEquals(1, result.size());
    assertEquals(1, result.getFirst().activities().size());
    assertEquals(5, result.getFirst().activities().getFirst().combinedAverageRating());
  }

  @Test
  void savePlanning_shouldCreateAndSavePlanningSuccessfully() {
    // Setup
    when(appUserService.getUserByProviderNameAndProviderUserId(authProviderName, userProviderId))
        .thenReturn(Optional.of(testAppUser));

    Set<String> activityIds = Set.of("activity1");
    when(activityService.findActivitiesByProviderNameAndIds(BookingProviderName.VIATOR, activityIds))
        .thenReturn(Set.of(testActivity));

    Planning savedPlanning = new Planning(testAppUser, "Test Planning");
    when(planningRepository.save(any(Planning.class))).thenReturn(savedPlanning);

    // Execute
    Planning result = planningService.savePlanning(validCreateRequest, authProviderName, userProviderId);

    // Verify
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Test Planning");
    assertThat(result.getAppUser()).isEqualTo(testAppUser);

    verify(activityService).cacheNewActivitiesByProvider(any());
    verify(activityService).findActivitiesByProviderNameAndIds(eq(BookingProviderName.VIATOR), eq(activityIds));

    // Capture and verify the Planning object passed to save()
    verify(planningRepository).save(planningCaptor.capture());
    Planning capturedPlanning = planningCaptor.getValue();
    assertThat(capturedPlanning.getName()).isEqualTo("Test Planning");
    assertThat(capturedPlanning.getAppUser()).isEqualTo(testAppUser);
    assertThat(capturedPlanning.getDayPlans()).hasSize(1);
  }

  @Test
  void savePlanning_shouldThrowWhenRequestIsNull() {
    assertThatThrownBy(() -> planningService.savePlanning(null, authProviderName, userProviderId))
        .isInstanceOf(PlanningCreationException.class)
        .hasMessageContaining("PlanningCreateRequestDTO or AuthProviderName or userProviderId cannot be null");
  }

  @Test
  void savePlanning_shouldThrowWhenAuthProviderNameIsNull() {
    assertThatThrownBy(() -> planningService.savePlanning(validCreateRequest, null, userProviderId))
        .isInstanceOf(PlanningCreationException.class)
        .hasMessageContaining("PlanningCreateRequestDTO or AuthProviderName or userProviderId cannot be null");
  }

  @Test
  void savePlanning_shouldThrowWhenUserProviderIdIsNull() {
    assertThatThrownBy(() -> planningService.savePlanning(validCreateRequest, authProviderName, null))
        .isInstanceOf(PlanningCreationException.class)
        .hasMessageContaining("PlanningCreateRequestDTO or AuthProviderName or userProviderId cannot be null");
  }

  @Test
  void savePlanning_shouldThrowWhenUserNotFound() {
    when(appUserService.getUserByProviderNameAndProviderUserId(authProviderName, userProviderId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> planningService.savePlanning(validCreateRequest, authProviderName, userProviderId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found for Planning Creation request");
  }

  @Test
  void savePlanning_shouldThrowWhenPlanningNameAlreadyExists() {
    when(appUserService.getUserByProviderNameAndProviderUserId(authProviderName, userProviderId))
        .thenReturn(Optional.of(testAppUser));

    when(planningRepository.existsByAppUserIdAndName(testAppUser.getId(), "Test Planning")).thenReturn(true);

    assertThatThrownBy(() -> planningService.savePlanning(validCreateRequest, authProviderName, userProviderId))
        .isInstanceOf(PlanningCreationException.class)
        .hasMessageContaining("Planning with the same name already exists");

    verify(planningRepository).existsByAppUserIdAndName(testAppUser.getId(), "Test Planning");

    verify(planningRepository, never()).save(any(Planning.class));
  }

  @Test
  void savePlanning_shouldThrowWhenActivityLookupMapIsEmpty() {
    when(appUserService.getUserByProviderNameAndProviderUserId(authProviderName, userProviderId))
        .thenReturn(Optional.of(testAppUser));

    when(activityService.findActivitiesByProviderNameAndIds(any(), any()))
        .thenReturn(Set.of()); // Empty set of activities

    assertThatThrownBy(() -> planningService.savePlanning(validCreateRequest, authProviderName, userProviderId))
        .isInstanceOf(PlanningCreationException.class)
        .hasMessageContaining("Error fetching any activities for the provided planning request.");
  }

  @Test
  void savePlanning_shouldThrowWhenActivityServiceThrowsException() {
    when(appUserService.getUserByProviderNameAndProviderUserId(authProviderName, userProviderId))
        .thenReturn(Optional.of(testAppUser));

    doThrow(new RuntimeException("Activity service error")).when(activityService).cacheNewActivitiesByProvider(any());

    assertThatThrownBy(() -> planningService.savePlanning(validCreateRequest, authProviderName, userProviderId))
        .isInstanceOf(PlanningCreationException.class)
        .hasMessageContaining("Error during caching new activities for Planning creation");
  }

  @Test
  void savePlanning_shouldSkipInvalidActivities() {
    // Setup invalid activity with startTime after endTime
    LocalDateTime startTime = LocalDateTime.now().plusHours(2);
    LocalDateTime endTime = startTime.minusHours(1); // end before start (invalid)

    PlanningCreateRequestDTO.CreateDayActivityDTO invalidActivity =
        new PlanningCreateRequestDTO.CreateDayActivityDTO(
            "invalidActivity", BookingProviderName.VIATOR, startTime, endTime);

    PlanningCreateRequestDTO.CreateDayActivityDTO validActivity =
        validCreateRequest.dayPlans().get(0).activities().get(0);

    PlanningCreateRequestDTO.CreateDayPlanDTO dayPlan =
        new PlanningCreateRequestDTO.CreateDayPlanDTO(LocalDate.now(), List.of(validActivity, invalidActivity));

    PlanningCreateRequestDTO request = new PlanningCreateRequestDTO("Test Planning", List.of(dayPlan));

    when(appUserService.getUserByProviderNameAndProviderUserId(authProviderName, userProviderId))
        .thenReturn(Optional.of(testAppUser));

    Set<String> activityIds = Set.of("activity1", "invalidActivity");
    when(activityService.findActivitiesByProviderNameAndIds(BookingProviderName.VIATOR, activityIds))
        .thenReturn(Set.of(testActivity));

    Planning savedPlanning = new Planning(testAppUser, "Test Planning");
    when(planningRepository.save(any(Planning.class))).thenReturn(savedPlanning);

    // Execute
    Planning result = planningService.savePlanning(request, authProviderName, userProviderId);

    // Verify
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Test Planning");

    // Capture and verify the Planning object passed to save()
    verify(planningRepository).save(planningCaptor.capture());
    Planning capturedPlanning = planningCaptor.getValue();

    // Verify planning properties
    assertThat(capturedPlanning.getName()).isEqualTo("Test Planning");
    assertThat(capturedPlanning.getAppUser()).isEqualTo(testAppUser);

    // Verify day plans
    assertThat(capturedPlanning.getDayPlans()).hasSize(1);

    // Verify activities in day plan - only the valid one should be included
    DayPlan capturedDayPlan = capturedPlanning.getDayPlans().iterator().next();
    assertThat(capturedDayPlan.getDayActivities()).hasSize(1);

    // Verify the activity is the valid one
    DayActivity capturedActivity = capturedDayPlan.getDayActivities().iterator().next();
    assertThat(capturedActivity.getActivity()).isEqualTo(testActivity);
    assertThat(capturedActivity.getStartTime()).isEqualTo(validActivity.startTime());
    assertThat(capturedActivity.getEndTime()).isEqualTo(validActivity.endTime());
  }

  @Test
  void savePlanning_shouldThrowWhenPlanningRepositoryThrowsException() {
    when(appUserService.getUserByProviderNameAndProviderUserId(authProviderName, userProviderId))
        .thenReturn(Optional.of(testAppUser));

    Set<String> activityIds = Set.of("activity1");
    when(activityService.findActivitiesByProviderNameAndIds(BookingProviderName.VIATOR, activityIds))
        .thenReturn(Set.of(testActivity));

    when(planningRepository.save(any(Planning.class))).thenThrow(new RuntimeException("Database error"));

    assertThatThrownBy(() -> planningService.savePlanning(validCreateRequest, authProviderName, userProviderId))
        .isInstanceOf(PlanningCreationException.class);
  }

  @Test
  void savePlanning_shouldHandleMultipleActivitiesAcrossMultipleDays() {
    // Setup
    when(appUserService.getUserByProviderNameAndProviderUserId(authProviderName, userProviderId))
        .thenReturn(Optional.of(testAppUser));

    Activity secondActivity =
        new Activity(
            "activity2", new BookingProvider(1L, BookingProviderName.VIATOR), 4.2f, 80, 120, "https://example2.com");

    Set<String> activityIds = Set.of("activity1", "activity2");
    when(activityService.findActivitiesByProviderNameAndIds(BookingProviderName.VIATOR, activityIds))
        .thenReturn(Set.of(testActivity, secondActivity));

    LocalDateTime day1StartTime = LocalDateTime.now().plusHours(1);
    LocalDateTime day1EndTime = day1StartTime.plusHours(1);

    LocalDateTime day2StartTime = LocalDateTime.now().plusDays(1).plusHours(2);
    LocalDateTime day2EndTime = day2StartTime.plusHours(2);

    PlanningCreateRequestDTO.CreateDayActivityDTO activity1 =
        new PlanningCreateRequestDTO.CreateDayActivityDTO(
            "activity1", BookingProviderName.VIATOR, day1StartTime, day1EndTime);

    PlanningCreateRequestDTO.CreateDayActivityDTO activity2 =
        new PlanningCreateRequestDTO.CreateDayActivityDTO(
            "activity2", BookingProviderName.VIATOR, day2StartTime, day2EndTime);

    PlanningCreateRequestDTO.CreateDayPlanDTO dayPlan1 =
        new PlanningCreateRequestDTO.CreateDayPlanDTO(LocalDate.now(), List.of(activity1));

    PlanningCreateRequestDTO.CreateDayPlanDTO dayPlan2 =
        new PlanningCreateRequestDTO.CreateDayPlanDTO(LocalDate.now().plusDays(1), List.of(activity2));

    PlanningCreateRequestDTO multiDayRequest =
        new PlanningCreateRequestDTO("Two-Day Planning", List.of(dayPlan1, dayPlan2));

    Planning savedPlanning = new Planning(testAppUser, "Two-Day Planning");
    when(planningRepository.save(any(Planning.class))).thenReturn(savedPlanning);

    // Execute
    Planning result = planningService.savePlanning(multiDayRequest, authProviderName, userProviderId);

    // Verify
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Two-Day Planning");

    // Capture and verify the Planning object passed to save()
    verify(planningRepository).save(planningCaptor.capture());
    Planning capturedPlanning = planningCaptor.getValue();

    // Verify Planning properties
    assertThat(capturedPlanning.getName()).isEqualTo("Two-Day Planning");
    assertThat(capturedPlanning.getAppUser()).isEqualTo(testAppUser);
    assertThat(capturedPlanning.getDayPlans()).hasSize(2);

    // Get day plans and sort them by date
    List<DayPlan> sortedDayPlans = new ArrayList<>(capturedPlanning.getDayPlans());
    sortedDayPlans.sort(Comparator.comparing(DayPlan::getDate));

    // Verify first day plan
    DayPlan firstDayPlan = sortedDayPlans.get(0);
    assertThat(firstDayPlan.getDate()).isEqualTo(LocalDate.now());
    assertThat(firstDayPlan.getDayActivities()).hasSize(1);

    // Verify first day activity
    DayActivity firstDayActivity = firstDayPlan.getDayActivities().iterator().next();
    assertThat(firstDayActivity.getActivity()).isEqualTo(testActivity);
    assertThat(firstDayActivity.getStartTime()).isEqualTo(day1StartTime);
    assertThat(firstDayActivity.getEndTime()).isEqualTo(day1EndTime);

    // Verify second day plan
    DayPlan secondDayPlan = sortedDayPlans.get(1);
    assertThat(secondDayPlan.getDate()).isEqualTo(LocalDate.now().plusDays(1));
    assertThat(secondDayPlan.getDayActivities()).hasSize(1);

    // Verify second day activity
    DayActivity secondDayActivity = secondDayPlan.getDayActivities().iterator().next();
    assertThat(secondDayActivity.getActivity()).isEqualTo(secondActivity);
    assertThat(secondDayActivity.getStartTime()).isEqualTo(day2StartTime);
    assertThat(secondDayActivity.getEndTime()).isEqualTo(day2EndTime);

    // Verify activities lookup
    verify(activityService).findActivitiesByProviderNameAndIds(eq(BookingProviderName.VIATOR), eq(activityIds));
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
        List.of(createTestCommonActivity(4.5)),
        new ActivityPlanningData(availability, startTimes, new int[] {5}, new int[] {1}),
        today);
  }

  private CommonActivity createTestCommonActivity(double rating) {
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

  private AppUser createTestUser() {
    testAppUser = new AppUser();
    testAppUser.setEmail("test@example.com");

    return testAppUser;
  }

  private Activity createTestActivity() {
    testActivity =
        new Activity(
            "activity1", new BookingProvider(1L, BookingProviderName.VIATOR), 4.5f, 100, 60, "https://example.com");

    return testActivity;
  }

  private PlanningCreateRequestDTO.CreateDayActivityDTO createTestDayActivityDTO(
      LocalDateTime startTime, LocalDateTime endTime) {
    return new PlanningCreateRequestDTO.CreateDayActivityDTO(
        "activity1", BookingProviderName.VIATOR, startTime, endTime);
  }

  private PlanningCreateRequestDTO.CreateDayPlanDTO createTestDayPlanDTO(
      LocalDate date, List<PlanningCreateRequestDTO.CreateDayActivityDTO> activities) {
    return new PlanningCreateRequestDTO.CreateDayPlanDTO(date, activities);
  }

  private PlanningCreateRequestDTO createTestPlanningCreateRequestDTO(
      List<PlanningCreateRequestDTO.CreateDayPlanDTO> dayPlans) {
    return new PlanningCreateRequestDTO("Test Planning", dayPlans);
  }
}
