package com.asialocalguide.gateway.viator.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.planning.CommonPersistableActivity;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningData;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningRequest;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorActivityAvailabilityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDetailDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivitySearchDTO;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ViatorActivityServiceTest {

  @Mock private ViatorClient viatorClient;

  @InjectMocks private ViatorActivityService service;

  private ProviderPlanningRequest validRequest;
  private final LocalDate today = LocalDate.now();
  private final LocalDate tomorrow = today.plusDays(1);

  @BeforeEach
  void setup() {
    validRequest = new ProviderPlanningRequest(today, tomorrow, 2, List.of("123"), "456", LanguageCode.EN);
  }

  @Test
  void fetchProviderPlanningData_shouldThrowWhenInvalidDestinationId() {
    ProviderPlanningRequest invalidRequest =
        new ProviderPlanningRequest(
            today,
            tomorrow,
            2,
            List.of("123"),
            "invalid", // Non-numeric destination ID
            LanguageCode.EN);

    assertThrows(IllegalArgumentException.class, () -> service.fetchProviderPlanningData(invalidRequest));
  }

  @Test
  void fetchProviderActivityData_shouldHandleEmptyPlanningList() {
    when(viatorClient.getActivitiesByRequestAndLanguage(anyString(), any())).thenReturn(Collections.emptyList());

    ProviderPlanningData result = service.fetchProviderPlanningData(validRequest);

    assertTrue(result.activities().isEmpty());
    assertNotNull(result.activityPlanningData());
  }

  @Test
  void fetchProviderPlanningData_shouldFilterZeroDurationActivities() {
    ViatorActivityDTO validActivity = createTestActivity(60);
    ViatorActivityDTO invalidActivity = createTestActivity(0);

    ViatorActivityAvailabilityDTO.BookableItem bookableItem =
        new ViatorActivityAvailabilityDTO.BookableItem(
            "opt1",
            List.of(
                new ViatorActivityAvailabilityDTO.Season(
                    "2023-01-01",
                    "2023-12-31",
                    List.of(
                        new ViatorActivityAvailabilityDTO.PricingRecord(
                            List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"),
                            List.of(new ViatorActivityAvailabilityDTO.TimedEntry("10:00", List.of())))))));

    when(viatorClient.getActivitiesByRequestAndLanguage(anyString(), any()))
        .thenReturn(List.of(validActivity, invalidActivity));
    when(viatorClient.getAvailabilityByProductCode(anyString()))
        .thenReturn(
            Optional.of(
                new ViatorActivityAvailabilityDTO(
                    validActivity.productCode(),
                    List.of(bookableItem),
                    "EUR",
                    new ViatorActivityAvailabilityDTO.Summary(50))));

    ProviderPlanningData result = service.fetchProviderPlanningData(validRequest);

    assertEquals(1, result.activities().size());
  }

  @Test
  void fetchProviderActivityData_shouldHandleAvailabilityFetchErrors() {
    ViatorActivityDTO activity = createTestActivity(60);

    when(viatorClient.getActivitiesByRequestAndLanguage(anyString(), any())).thenReturn(List.of(activity));
    when(viatorClient.getAvailabilityByProductCode(anyString())).thenReturn(Optional.empty());

    ProviderPlanningData result = service.fetchProviderPlanningData(validRequest);

    assertTrue(result.activities().isEmpty());
  }

  @Test
  void fetchProviderPlanningData_shouldHandlePartialAvailabilityFailures() {
    ViatorActivityDTO activity1 = createTestActivity(60);
    ViatorActivityDTO activity2 = createTestActivity(90);

    when(viatorClient.getActivitiesByRequestAndLanguage(anyString(), any())).thenReturn(List.of(activity1, activity2));
    when(viatorClient.getAvailabilityByProductCode(activity1.productCode()))
        .thenReturn(
            Optional.of(
                new ViatorActivityAvailabilityDTO(
                    activity1.productCode(),
                    List.of(
                        new ViatorActivityAvailabilityDTO.BookableItem(
                            "opt1",
                            List.of(
                                new ViatorActivityAvailabilityDTO.Season(
                                    "2023-01-01",
                                    "2023-12-31",
                                    List.of(
                                        new ViatorActivityAvailabilityDTO.PricingRecord(
                                            List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"),
                                            List.of(
                                                new ViatorActivityAvailabilityDTO.TimedEntry("10:00", List.of())))))))),
                    "EUR",
                    new ViatorActivityAvailabilityDTO.Summary(50))));
    when(viatorClient.getAvailabilityByProductCode(activity2.productCode())).thenReturn(Optional.empty());

    ProviderPlanningData result = service.fetchProviderPlanningData(validRequest);

    assertEquals(1, result.activities().size());
  }

  @Test
  void fetchProviderPlanningData_shouldHandleAsyncFailures() {
    ViatorActivityDTO activity = createTestActivity(60);

    when(viatorClient.getActivitiesByRequestAndLanguage(anyString(), any())).thenReturn(List.of(activity));
    when(viatorClient.getAvailabilityByProductCode(anyString())).thenThrow(new RuntimeException("Simulated failure"));

    ProviderPlanningData providerData = service.fetchProviderPlanningData(validRequest);

    assertNotNull(providerData);
  }

  @Test
  void fetchProviderPlanningData_shouldFilterAndConvertTags() {
    // Given
    ProviderPlanningRequest request =
        new ProviderPlanningRequest(
            today,
            tomorrow,
            2,
            List.of("123", "invalid", "456"), // Mixed valid/invalid tags
            "456",
            LanguageCode.EN);

    // Fix the stubbing by using specific argument matchers
    when(viatorClient.getActivitiesByRequestAndLanguage(
            eq(LanguageCode.EN.toString()), any(ViatorActivitySearchDTO.class)))
        .thenReturn(Collections.emptyList());

    // When
    service.fetchProviderPlanningData(request);

    // Then - Verify tags filtering and conversion
    ArgumentCaptor<ViatorActivitySearchDTO> searchCaptor = ArgumentCaptor.forClass(ViatorActivitySearchDTO.class);

    verify(viatorClient).getActivitiesByRequestAndLanguage(eq(LanguageCode.EN.toString()), searchCaptor.capture());

    ViatorActivitySearchDTO actualSearch = searchCaptor.getValue();
    assertEquals(List.of(123, 456), actualSearch.filtering().tags());
  }

  @Test
  void fetchProviderPlanningData_shouldCalculatePaginationFromDates() {
    // Given
    LocalDate endDate = today.plusDays(3); // 3-day duration
    int expectedItemsPerPage = 12; // 3 days * 4 activities/day

    ProviderPlanningRequest request =
        new ProviderPlanningRequest(
            today,
            endDate,
            4, // This value is actually ignored in current implementation
            List.of("123"),
            "456",
            LanguageCode.EN);

    when(viatorClient.getActivitiesByRequestAndLanguage(anyString(), any())).thenReturn(Collections.emptyList());

    // When
    service.fetchProviderPlanningData(request);

    // Then - Verify pagination calculation
    ArgumentCaptor<ViatorActivitySearchDTO> searchCaptor = ArgumentCaptor.forClass(ViatorActivitySearchDTO.class);

    verify(viatorClient).getActivitiesByRequestAndLanguage(eq(LanguageCode.EN.toString()), searchCaptor.capture());

    ViatorActivitySearchDTO actualSearch = searchCaptor.getValue();
    assertEquals(expectedItemsPerPage, actualSearch.pagination().count());
  }

  @Test
  void validatePlanningRequest_shouldRejectBackwardsDates() {
    ProviderPlanningRequest invalidRequest =
        new ProviderPlanningRequest(tomorrow, today, 2, List.of("123"), "456", LanguageCode.EN);

    assertThrows(IllegalArgumentException.class, () -> service.fetchProviderPlanningData(invalidRequest));
  }

  @Test
  void convertActivityTags_shouldHandleNullTags() {
    // Given
    ProviderPlanningRequest request =
        new ProviderPlanningRequest(
            today,
            tomorrow,
            2,
            null, // Null tags
            "456",
            LanguageCode.EN);

    // When
    service.fetchProviderPlanningData(request);

    ArgumentCaptor<ViatorActivitySearchDTO> searchCaptor = ArgumentCaptor.forClass(ViatorActivitySearchDTO.class);

    verify(viatorClient).getActivitiesByRequestAndLanguage(eq(LanguageCode.EN.toString()), searchCaptor.capture());

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
            List.of(new ViatorActivityDTO.ReviewsDTO.SourceDTO("viator", 100, 4.5)), 100, 4.5),
        new ViatorActivityDTO.DurationDTO(null, durationMinutes, null),
        "CONFIRMATION",
        "ITINERARY",
        new ViatorActivityDTO.PricingDTO(new ViatorActivityDTO.PricingDTO.SummaryDTO(50.0, 60.0), "EUR"),
        "http://test.com",
        List.of(),
        List.of(123),
        List.of(),
        null);
  }

  @Test
  void fetchProviderActivities_shouldFetchAndMapActivitiesSuccessfully() {
    // Given
    Set<String> activityIds = Set.of("product1", "product2");

    // Mock English responses (required)
    ViatorActivityDetailDTO enDto1 = createDetailDTO("product1", "en", "Activity 1", "Description 1");
    ViatorActivityDetailDTO enDto2 = createDetailDTO("product2", "en", "Activity 2", "Description 2");

    // Mock French responses
    ViatorActivityDetailDTO frDto1 = createDetailDTO("product1", "fr", "Activité 1", "Description en français");
    ViatorActivityDetailDTO frDto2 = createDetailDTO("product2", "fr", "Activité 2", "Description en français");

    when(viatorClient.getActivityByIdAndLanguage("en", "product1")).thenReturn(Optional.of(enDto1));
    when(viatorClient.getActivityByIdAndLanguage("en", "product2")).thenReturn(Optional.of(enDto2));
    when(viatorClient.getActivityByIdAndLanguage("fr", "product1")).thenReturn(Optional.of(frDto1));
    when(viatorClient.getActivityByIdAndLanguage("fr", "product2")).thenReturn(Optional.of(frDto2));

    // When
    List<CommonPersistableActivity> result = service.fetchProviderActivities(activityIds);

    // Then
    assertEquals(2, result.size());

    // Verify product1
    CommonPersistableActivity activity1 = findActivityById(result, "product1");
    assertNotNull(activity1);

    assertEquals(2, activity1.title().size()); // EN and FR
    assertTrue(hasTranslation(activity1.title(), LanguageCode.EN, "Activity 1"));
    assertTrue(hasTranslation(activity1.title(), LanguageCode.FR, "Activité 1"));

    // Verify product2
    CommonPersistableActivity activity2 = findActivityById(result, "product2");
    assertNotNull(activity2);
    assertEquals(2, activity2.title().size()); // EN and FR
    assertTrue(hasTranslation(activity2.title(), LanguageCode.EN, "Activity 2"));
    assertTrue(hasTranslation(activity2.title(), LanguageCode.FR, "Activité 2"));
  }

  @Test
  void fetchProviderActivities_shouldHandleIndividualLanguageFailures() {
    // Given
    Set<String> activityIds = Set.of("product1");

    // Mock English success
    ViatorActivityDetailDTO enDto = createDetailDTO("product1", "en", "Activity", "Description");
    when(viatorClient.getActivityByIdAndLanguage("en", "product1")).thenReturn(Optional.of(enDto));

    // Mock French to throw exception
    when(viatorClient.getActivityByIdAndLanguage("fr", "product1")).thenThrow(new RuntimeException("API error"));

    // When
    List<CommonPersistableActivity> result = service.fetchProviderActivities(activityIds);

    // Then
    assertEquals(1, result.size());
    assertEquals(1, result.get(0).title().size()); // Only English translation
    assertEquals(LanguageCode.EN, result.get(0).title().get(0).languageCode());
  }

  @Test
  void fetchProviderActivities_shouldMapImagesCorrectly() {
    // Given
    Set<String> activityIds = Set.of("product1");

    // Create DTO with images - must include English version
    ViatorActivityDetailDTO dto = createDetailDTOWithImages("product1", "en");
    when(viatorClient.getActivityByIdAndLanguage("en", "product1")).thenReturn(Optional.of(dto));

    // Mock French to return empty to avoid interference
    when(viatorClient.getActivityByIdAndLanguage("fr", "product1")).thenReturn(Optional.empty());

    // When
    List<CommonPersistableActivity> result = service.fetchProviderActivities(activityIds);

    // Then
    assertEquals(1, result.size());

    // Verify images
    List<CommonPersistableActivity.Image> images = result.get(0).images();
    assertEquals(2, images.size());

    // Verify mobile image
    Optional<CommonPersistableActivity.Image> mobileImage =
        images.stream().filter(img -> img.type() == CommonPersistableActivity.ImageType.MOBILE).findFirst();
    assertTrue(mobileImage.isPresent());
    assertEquals(320, mobileImage.get().height());
    assertEquals(480, mobileImage.get().width());

    // Verify desktop image
    Optional<CommonPersistableActivity.Image> desktopImage =
        images.stream().filter(img -> img.type() == CommonPersistableActivity.ImageType.DESKTOP).findFirst();
    assertTrue(desktopImage.isPresent());
    assertEquals(480, desktopImage.get().height());
    assertEquals(720, desktopImage.get().width());
  }

  @Test
  void fetchProviderActivities_shouldHandleNullImagesGracefully() {
    // Given
    Set<String> activityIds = Set.of("product1");

    // Create DTO with null images - must include English version
    ViatorActivityDetailDTO dto = createDetailDTOWithNullImages("product1", "en");
    when(viatorClient.getActivityByIdAndLanguage("en", "product1")).thenReturn(Optional.of(dto));

    // Mock French to return empty to avoid interference
    when(viatorClient.getActivityByIdAndLanguage("fr", "product1")).thenReturn(Optional.empty());

    // When
    List<CommonPersistableActivity> result = service.fetchProviderActivities(activityIds);

    // Then
    assertEquals(1, result.size());
    assertTrue(result.get(0).images().isEmpty());
  }

  @Test
  void fetchProviderActivities_shouldMapReviewsCorrectly() {
    // Given
    Set<String> activityIds = Set.of("product1");

    // Create DTO with reviews - must include English version
    ViatorActivityDetailDTO dto = createDetailDTOWithReviews("product1", "en", 4.5, 100);
    when(viatorClient.getActivityByIdAndLanguage("en", "product1")).thenReturn(Optional.of(dto));

    // Mock French to return empty to avoid interference
    when(viatorClient.getActivityByIdAndLanguage("fr", "product1")).thenReturn(Optional.empty());

    // When
    List<CommonPersistableActivity> result = service.fetchProviderActivities(activityIds);

    // Then
    assertEquals(1, result.size());
    CommonPersistableActivity.Review review = result.get(0).review();
    assertEquals(4.5, review.averageRating()); // Using the actual rating value
    assertEquals(100, review.reviewCount());
  }

  @Test
  void fetchProviderActivities_shouldHandleNullReviews() {
    // Given
    Set<String> activityIds = Set.of("product1");

    // Create DTO with null reviews - must include English version
    ViatorActivityDetailDTO dto = createDetailDTOWithNullReviews("product1", "en");
    when(viatorClient.getActivityByIdAndLanguage("en", "product1")).thenReturn(Optional.of(dto));

    // Mock French to return empty to avoid interference
    when(viatorClient.getActivityByIdAndLanguage("fr", "product1")).thenReturn(Optional.empty());

    // When
    List<CommonPersistableActivity> result = service.fetchProviderActivities(activityIds);

    // Then
    assertEquals(1, result.size());
    assertEquals(1.0, result.get(0).review().averageRating()); // Default value
    assertEquals(1, result.get(0).review().reviewCount()); // Default value
  }

  private ViatorActivityDetailDTO createDetailDTO(
      String productCode, String language, String title, String description) {
    return new ViatorActivityDetailDTO(
        productCode,
        language,
        title,
        description,
        List.of(), // empty images
        List.of(), // empty tags
        List.of(), // empty destinations
        new ViatorActivityDetailDTO.ItineraryDTO(
            "STANDARD", new ViatorActivityDetailDTO.DurationDTO(null, null, 60) // 60 minute duration
            ),
        "http://viator.com/activity/" + productCode, // URL with product code
        new ViatorActivityDetailDTO.ReviewsDTO(List.of(), 100, 4.5) // Default reviews
        );
  }

  private CommonPersistableActivity findActivityById(List<CommonPersistableActivity> result, String id) {
    return result.stream().filter(activity -> activity.providerId().equals(id)).findFirst().orElse(null);
  }

  private boolean hasTranslation(
      List<CommonPersistableActivity.Translation> translations, LanguageCode lang, String value) {
    return translations.stream().anyMatch(t -> t.languageCode() == lang && t.value().equals(value));
  }

  private ViatorActivityDetailDTO createDetailDTOWithImages(String productCode, String language) {
    List<ViatorActivityDetailDTO.ImageVariantDTO> variants =
        List.of(
            new ViatorActivityDetailDTO.ImageVariantDTO(320, 480, "http://test.com/image_mobile.jpg"),
            new ViatorActivityDetailDTO.ImageVariantDTO(480, 720, "http://test.com/image_desktop.jpg"));

    ViatorActivityDetailDTO.ImageDTO coverImage =
        new ViatorActivityDetailDTO.ImageDTO("viator", "Test caption", true, variants);

    return new ViatorActivityDetailDTO(
        productCode,
        language,
        "Test Activity",
        "Test Description",
        List.of(coverImage), // Include the cover image
        List.of(),
        List.of(),
        new ViatorActivityDetailDTO.ItineraryDTO("STANDARD", new ViatorActivityDetailDTO.DurationDTO(null, null, 60)),
        "http://viator.com/activity/" + productCode,
        new ViatorActivityDetailDTO.ReviewsDTO(List.of(), 100, 4.5));
  }

  private ViatorActivityDetailDTO createDetailDTOWithNullImages(String productCode, String language) {
    return new ViatorActivityDetailDTO(
        productCode,
        language,
        "Test Activity",
        "Test Description",
        null, // Null images
        List.of(),
        List.of(),
        new ViatorActivityDetailDTO.ItineraryDTO("STANDARD", new ViatorActivityDetailDTO.DurationDTO(null, null, 60)),
        "http://viator.com/activity/" + productCode,
        new ViatorActivityDetailDTO.ReviewsDTO(List.of(), 100, 4.5));
  }

  private ViatorActivityDetailDTO createDetailDTOWithReviews(
      String productCode, String language, double rating, int count) {
    return new ViatorActivityDetailDTO(
        productCode,
        language,
        "Test Activity",
        "Test Description",
        List.of(),
        List.of(),
        List.of(),
        new ViatorActivityDetailDTO.ItineraryDTO("STANDARD", new ViatorActivityDetailDTO.DurationDTO(null, null, 60)),
        "http://viator.com/activity/" + productCode,
        new ViatorActivityDetailDTO.ReviewsDTO(List.of(), count, rating) // Convert to integer representation
        );
  }

  private ViatorActivityDetailDTO createDetailDTOWithNullReviews(String productCode, String language) {
    return new ViatorActivityDetailDTO(
        productCode,
        language,
        "Test Activity",
        "Test Description",
        List.of(),
        List.of(),
        List.of(),
        new ViatorActivityDetailDTO.ItineraryDTO("STANDARD", new ViatorActivityDetailDTO.DurationDTO(null, null, 60)),
        "http://viator.com/activity/" + productCode,
        null // Null reviews
        );
  }
}
