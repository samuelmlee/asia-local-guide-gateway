package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.planning.ProviderActivityData;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningRequest;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorActivityAvailabilityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivitySearchDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViatorActivityServiceTest {

    @Mock
    private ViatorClient viatorClient;

    @InjectMocks
    private ViatorActivityService service;

    private ProviderPlanningRequest validRequest;
    private final LocalDate today = LocalDate.now();
    private final LocalDate tomorrow = today.plusDays(1);

    @BeforeEach
    void setup() {
        validRequest = new ProviderPlanningRequest(
                today,
                tomorrow,
                2,
                List.of("123"),
                "456",
                LanguageCode.EN
        );
    }

    @Test
    void fetchProviderActivityData_shouldThrowWhenInvalidDestinationId() {
        ProviderPlanningRequest invalidRequest = new ProviderPlanningRequest(
                today,
                tomorrow,
                2,
                List.of("123"),
                "invalid", // Non-numeric destination ID
                LanguageCode.EN
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.fetchProviderActivityData(invalidRequest));
    }

    @Test
    void fetchProviderActivityData_shouldHandleEmptyActivityList() {
        when(viatorClient.getActivitiesByRequestAndLanguage(anyString(), any()))
                .thenReturn(Collections.emptyList());

        ProviderActivityData result = service.fetchProviderActivityData(validRequest);

        assertTrue(result.activities().isEmpty());
        assertNotNull(result.activityData());
    }

    @Test
    void fetchProviderActivityData_shouldFilterZeroDurationActivities() {
        ViatorActivityDTO validActivity = createTestActivity(60);
        ViatorActivityDTO invalidActivity = createTestActivity(0);

        when(viatorClient.getActivitiesByRequestAndLanguage(anyString(), any()))
                .thenReturn(List.of(validActivity, invalidActivity));
        when(viatorClient.getAvailabilityByProductCode(anyString()))
                .thenReturn(Optional.of(new ViatorActivityAvailabilityDTO(validActivity.productCode(), List.of(new ViatorActivityAvailabilityDTO.BookableItem("opt1", List.of())), "EUR", new ViatorActivityAvailabilityDTO.Summary(50))));

        ProviderActivityData result = service.fetchProviderActivityData(validRequest);

        assertEquals(1, result.activities().size());
    }

    @Test
    void fetchProviderActivityData_shouldHandleAvailabilityFetchErrors() {
        ViatorActivityDTO activity = createTestActivity(60);

        when(viatorClient.getActivitiesByRequestAndLanguage(anyString(), any()))
                .thenReturn(List.of(activity));
        when(viatorClient.getAvailabilityByProductCode(anyString()))
                .thenReturn(Optional.empty());

        ProviderActivityData result = service.fetchProviderActivityData(validRequest);

        assertTrue(result.activities().isEmpty());
    }

    @Test
    void fetchProviderActivityData_shouldHandlePartialAvailabilityFailures() {
        ViatorActivityDTO activity1 = createTestActivity(60);
        ViatorActivityDTO activity2 = createTestActivity(90);

        when(viatorClient.getActivitiesByRequestAndLanguage(anyString(), any()))
                .thenReturn(List.of(activity1, activity2));
        when(viatorClient.getAvailabilityByProductCode(activity1.productCode()))
                .thenReturn(Optional.of(new ViatorActivityAvailabilityDTO(activity1.productCode(), List.of(new ViatorActivityAvailabilityDTO.BookableItem("opt1", List.of())), "EUR", new ViatorActivityAvailabilityDTO.Summary(50))));
        when(viatorClient.getAvailabilityByProductCode(activity2.productCode()))
                .thenReturn(Optional.empty());

        ProviderActivityData result = service.fetchProviderActivityData(validRequest);

        assertEquals(1, result.activities().size());
    }

    @Test
    void fetchProviderActivityData_shouldHandleAsyncFailures() {
        ViatorActivityDTO activity = createTestActivity(60);

        when(viatorClient.getActivitiesByRequestAndLanguage(anyString(), any()))
                .thenReturn(List.of(activity));
        when(viatorClient.getAvailabilityByProductCode(anyString()))
                .thenThrow(new RuntimeException("Simulated failure"));

        ProviderActivityData providerData = service.fetchProviderActivityData(validRequest);

        assertNotNull(providerData);
    }

    @Test
    void fetchProviderActivityData_shouldFilterAndConvertTags() {
        // Given
        ProviderPlanningRequest request = new ProviderPlanningRequest(
                today,
                tomorrow,
                2,
                List.of("123", "invalid", "456"), // Mixed valid/invalid tags
                "456",
                LanguageCode.EN
        );

        when(viatorClient.getActivitiesByRequestAndLanguage(anyString(), any()))
                .thenReturn(Collections.emptyList());

        // When
        service.fetchProviderActivityData(request);

        // Then - Verify tags filtering and conversion
        ArgumentCaptor<ViatorActivitySearchDTO> searchCaptor =
                ArgumentCaptor.forClass(ViatorActivitySearchDTO.class);

        verify(viatorClient).getActivitiesByRequestAndLanguage(
                eq(LanguageCode.EN.toString()),
                searchCaptor.capture()
        );

        ViatorActivitySearchDTO actualSearch = searchCaptor.getValue();
        assertEquals(List.of(123, 456), actualSearch.filtering().tags());
    }

    @Test
    void fetchProviderActivityData_shouldCalculatePaginationFromDates() {
        // Given
        LocalDate endDate = today.plusDays(3); // 3-day duration
        int expectedItemsPerPage = 12; // 3 days * 4 activities/day

        ProviderPlanningRequest request = new ProviderPlanningRequest(
                today,
                endDate,
                4, // This value is actually ignored in current implementation
                List.of("123"),
                "456",
                LanguageCode.EN
        );

        when(viatorClient.getActivitiesByRequestAndLanguage(anyString(), any()))
                .thenReturn(Collections.emptyList());

        // When
        service.fetchProviderActivityData(request);

        // Then - Verify pagination calculation
        ArgumentCaptor<ViatorActivitySearchDTO> searchCaptor =
                ArgumentCaptor.forClass(ViatorActivitySearchDTO.class);

        verify(viatorClient).getActivitiesByRequestAndLanguage(
                eq(LanguageCode.EN.toString()),
                searchCaptor.capture()
        );

        ViatorActivitySearchDTO actualSearch = searchCaptor.getValue();
        assertEquals(expectedItemsPerPage, actualSearch.pagination().count());
    }

    @Test
    void validatePlanningRequest_shouldRejectBackwardsDates() {
        ProviderPlanningRequest invalidRequest = new ProviderPlanningRequest(
                tomorrow,
                today,
                2,
                List.of("123"),
                "456",
                LanguageCode.EN
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.fetchProviderActivityData(invalidRequest));
    }

    @Test
    void convertActivityTags_shouldHandleNullTags() {
        // Given
        ProviderPlanningRequest request = new ProviderPlanningRequest(
                today,
                tomorrow,
                2,
                null,  // Null tags
                "456",
                LanguageCode.EN
        );

        // When
        service.fetchProviderActivityData(request);

        ArgumentCaptor<ViatorActivitySearchDTO> searchCaptor =
                ArgumentCaptor.forClass(ViatorActivitySearchDTO.class);

        verify(viatorClient).getActivitiesByRequestAndLanguage(
                eq(LanguageCode.EN.toString()),
                searchCaptor.capture()
        );

        ViatorActivitySearchDTO actualSearch = searchCaptor.getValue();
        assertTrue(actualSearch.filtering().tags().isEmpty());
    }

    private ViatorActivityDTO createTestActivity(int durationMinutes) {
        return new ViatorActivityDTO(
                "P" + UUID.randomUUID(),
                "Test Activity",
                "Description",
                List.of(),
                new ViatorActivityDTO.ReviewsDTO(
                        List.of(new ViatorActivityDTO.ReviewsDTO.SourceDTO("viator", 100, 4.5)),
                        100,
                        4.5
                ),
                new ViatorActivityDTO.DurationDTO(null, durationMinutes, null),
                "CONFIRMATION",
                "ITINERARY",
                new ViatorActivityDTO.PricingDTO(
                        new ViatorActivityDTO.PricingDTO.SummaryDTO(50.0, 60.0),
                        "EUR"
                ),
                "http://test.com",
                List.of(),
                List.of(123),
                List.of(),
                null
        );
    }
}