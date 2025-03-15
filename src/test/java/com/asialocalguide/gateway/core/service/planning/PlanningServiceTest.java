package com.asialocalguide.gateway.core.service.planning;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.ActivityData;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity;
import com.asialocalguide.gateway.core.domain.planning.ProviderActivityData;
import com.asialocalguide.gateway.core.dto.planning.DayPlanDTO;
import com.asialocalguide.gateway.core.dto.planning.PlanningRequestDTO;
import com.asialocalguide.gateway.core.service.strategy.FetchActivitiesStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanningServiceTest {

    @Mock
    private FetchActivitiesStrategy strategy1;

    @Mock
    private FetchActivitiesStrategy strategy2;

    private PlanningService planningService;

    private PlanningRequestDTO validRequest;
    private final LocalDate today = LocalDate.now();
    private final LocalDate tomorrow = today.plusDays(1);

    @BeforeEach
    void setup() {
        planningService = new PlanningService(List.of(strategy1, strategy2));

        validRequest = new PlanningRequestDTO(
                today,
                tomorrow,
                1L,
                List.of("adventure")
        );
    }

    @Test
    void generateActivityPlanning_shouldHandleNoProviders() {
        PlanningService service = new PlanningService(List.of());

        List<DayPlanDTO> result = service.generateActivityPlanning(validRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    void generateActivityPlanning_shouldHandleFailedProviders() {
        // Setup 2-day request
        LocalDate endDate = today.plusDays(1);
        PlanningRequestDTO request = new PlanningRequestDTO(
                today,
                endDate,
                1L,
                List.of("adventure")
        );

        when(strategy1.fetchProviderActivity(any(), any()))
                .thenThrow(new RuntimeException("Provider error"));
        when(strategy2.fetchProviderActivity(any(), any()))
                .thenReturn(createTestProviderData());

        List<DayPlanDTO> result = planningService.generateActivityPlanning(request);

        // Verify exactly one activity is scheduled across all days
        int totalActivities = result.stream()
                .mapToInt(day -> day.activities().size())
                .sum();
        assertEquals(1, totalActivities);
    }

    @Test
    void generateActivityPlanning_shouldHandleEmptyActivityData() {
        when(strategy1.fetchProviderActivity(any(), any()))
                .thenReturn(new ProviderActivityData(
                        List.of(),
                        new ActivityData(new boolean[0][0][0], new String[0][0][0], new int[0], new int[0]),
                        today
                ));

        List<DayPlanDTO> result = planningService.generateActivityPlanning(validRequest);

        assertTrue(result.stream().allMatch(day -> day.activities().isEmpty()));
    }

    @Test
    void generateActivityPlanning_shouldHandleSchedulingFailure() {
        // Setup invalid activity data that can't be scheduled
        ActivityData invalidData = new ActivityData(
                new boolean[][][]{{{true}}},  // Availability
                new String[][][]{{{""}}},     // Start times
                new int[]{1},                 // Ratings
                new int[]{1440}               // 24-hour duration
        );

        when(strategy1.fetchProviderActivity(any(), any()))
                .thenReturn(new ProviderActivityData(
                        List.of(createTestActivity()),
                        invalidData,
                        today
                ));

        List<DayPlanDTO> result = planningService.generateActivityPlanning(validRequest);

        assertTrue(result.getFirst().activities().isEmpty());
    }


    @Test
    void generateActivityPlanning_shouldCreateMultiDaySchedule() {
        // Setup activities available across 3 days
        ActivityData testData = new ActivityData(
                new boolean[][][]{ // 1 activity x 3 days x 1 slot
                        {{true}, {true}, {true}}
                },
                new String[][][]{ // Start times for each day
                        {{"09:00"}, {"14:00"}, {"18:00"}}
                },
                new int[]{5},
                new int[]{60}
        );

        when(strategy1.fetchProviderActivity(any(), any()))
                .thenReturn(new ProviderActivityData(
                        List.of(createTestActivity(), createTestActivity(), createTestActivity()),
                        testData,
                        today
                ));

        PlanningRequestDTO multiDayRequest = new PlanningRequestDTO(
                today,
                today.plusDays(2), // 3-day duration
                1L,
                List.of("adventure")
        );

        List<DayPlanDTO> result = planningService.generateActivityPlanning(multiDayRequest);

        assertEquals(3, result.size());
        assertEquals(3, result.stream()
                .mapToInt(day -> day.activities().size())
                .sum());
    }

    @Test
    void generateActivityPlanning_shouldHandleTimeSlotConflicts() {
        // Setup activities that would create scheduling conflicts
        ActivityData conflictData = new ActivityData(
                new boolean[][][]{ // 2 activities x 1 day x 2 slots
                        {{true, true}},
                        {{true, false}}
                },
                new String[][][]{
                        {{"09:00", "10:00"}},
                        {{"14:00", ""}}
                },
                new int[]{5, 4},
                new int[]{60, 120}
        );

        when(strategy1.fetchProviderActivity(any(), any()))
                .thenReturn(new ProviderActivityData(
                        List.of(createTestActivity(), createTestActivity()),
                        conflictData,
                        today
                ));

        List<DayPlanDTO> result = planningService.generateActivityPlanning(validRequest);

        // Verify the scheduler resolves conflicts by selecting higher-rated activity
        assertEquals(1, result.size());
        assertFalse(result.getFirst().activities().isEmpty());
    }

    private ProviderActivityData createTestProviderData() {
        // For 2-day request
        boolean[][][] availability = new boolean[1][2][24]; // 1 activity x 2 days x 1 slot
        availability[0][0][8] = true;
        availability[0][1][13] = true;

        String[][][] startTimes = new String[1][2][24]; // Match days
        startTimes[0][0][8] = "09:00";
        startTimes[0][1][13] = "14:00";

        return new ProviderActivityData(
                List.of(createTestActivity()),
                new ActivityData(
                        availability,
                        startTimes,
                        new int[]{5},
                        new int[]{1}
                ),
                today
        );
    }

    private CommonActivity createTestActivity() {
        return new CommonActivity(
                "Test Activity",
                "Test Description",
                List.of(),
                new CommonActivity.CommonReviews(4.5, 100),
                new CommonActivity.CommonDuration(60, 60),
                new CommonActivity.CommonPricing(50.0, "EUR"),
                "http://booking.com",
                List.of("adventure"),
                BookingProviderName.VIATOR,
                "VIATOR-123"
        );
    }
}

