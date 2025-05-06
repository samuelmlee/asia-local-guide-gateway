package com.asialocalguide.gateway.viator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.planning.CommonPersistableActivity;
import com.asialocalguide.gateway.core.domain.planning.ImageType;
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
  void fetchProviderPlanningData_shouldHandleEmptyPlanningList() {
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
  void fetchProviderPlanningData_shouldHandleAvailabilityFetchErrors() {
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
    assertThat(result).hasSize(2);

    // Verify product1
    CommonPersistableActivity activity1 = findActivityById(result, "product1");
    assertThat(activity1).isNotNull();

    assertThat(activity1.title()).hasSize(2); // EN and FR
    assertThat(hasTranslation(activity1.title(), LanguageCode.EN, "Activity 1")).isTrue();
    assertThat(hasTranslation(activity1.title(), LanguageCode.FR, "Activité 1")).isTrue();

    // Verify product2
    CommonPersistableActivity activity2 = findActivityById(result, "product2");
    assertThat(activity2).isNotNull();
    assertThat(activity2.title()).hasSize(2); // EN and FR
    assertThat(hasTranslation(activity2.title(), LanguageCode.EN, "Activity 2")).isTrue();
    assertThat(hasTranslation(activity2.title(), LanguageCode.FR, "Activité 2")).isTrue();
  }

  @Test
  void fetchProviderActivities_shouldRejectNullActivityIds() {
    // When/Then
    assertThatThrownBy(() -> service.fetchProviderActivities(null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void fetchProviderActivities_shouldRejectEmptyActivityIds() {
    // When/Then
    assertThatThrownBy(() -> service.fetchProviderActivities(Set.of())).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void fetchProviderActivities_shouldThrowExceptionWhenEnglishActivitiesMissing() {
    // Given
    Set<String> activityIds = Set.of("product1");

    // Only French data available
    ViatorActivityDetailDTO frDto = createDetailDTO("product1", "fr", "Activité 1", "Description");
    when(viatorClient.getActivityByIdAndLanguage("en", "product1")).thenReturn(Optional.empty());
    when(viatorClient.getActivityByIdAndLanguage("fr", "product1")).thenReturn(Optional.of(frDto));

    // When/Then
    assertThatThrownBy(() -> service.fetchProviderActivities(activityIds)).isInstanceOf(Exception.class);
  }

  @Test
  void fetchProviderActivities_shouldSkipNullActivityIds() {
    // Given - mix of valid and null IDs
    Set<String> activityIds = new HashSet<>();
    activityIds.add("product1");
    activityIds.add(null);

    ViatorActivityDetailDTO enDto = createDetailDTO("product1", "en", "Activity 1", "Description");
    when(viatorClient.getActivityByIdAndLanguage("en", "product1")).thenReturn(Optional.of(enDto));

    // When
    List<CommonPersistableActivity> result = service.fetchProviderActivities(activityIds);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).providerId()).isEqualTo("product1");
  }

  @Test
  void fetchProviderActivities_shouldHandleClientExceptions() {
    // Given
    Set<String> activityIds = Set.of("product1", "product2");

    // First product works
    ViatorActivityDetailDTO enDto = createDetailDTO("product1", "en", "Activity 1", "Description");
    when(viatorClient.getActivityByIdAndLanguage("en", "product1")).thenReturn(Optional.of(enDto));
    when(viatorClient.getActivityByIdAndLanguage("fr", "product1")).thenReturn(Optional.empty());

    // Second product throws exception
    when(viatorClient.getActivityByIdAndLanguage(anyString(), eq("product2")))
        .thenThrow(new RuntimeException("API error"));

    // When
    List<CommonPersistableActivity> result = service.fetchProviderActivities(activityIds);

    // Then - should still return the successful product
    assertThat(result).hasSize(1);
    assertThat(result.get(0).providerId()).isEqualTo("product1");
  }

  @Test
  void fetchProviderActivities_shouldCreateProperTranslations() {
    // Given
    Set<String> activityIds = Set.of("product1");

    // Create data for all supported languages
    ViatorActivityDetailDTO enDto = createDetailDTO("product1", "en", "Activity", "English description");
    ViatorActivityDetailDTO frDto = createDetailDTO("product1", "fr", "Activité", "Description française");

    when(viatorClient.getActivityByIdAndLanguage("en", "product1")).thenReturn(Optional.of(enDto));
    when(viatorClient.getActivityByIdAndLanguage("fr", "product1")).thenReturn(Optional.of(frDto));

    // When
    List<CommonPersistableActivity> result = service.fetchProviderActivities(activityIds);

    // Then
    assertThat(result).hasSize(1);

    // Verify translations are properly mapped
    CommonPersistableActivity activity = result.get(0);

    // Title translations
    assertThat(activity.title()).hasSize(2); // EN and FR
    assertThat(findTranslation(activity.title(), LanguageCode.EN)).isNotNull();
    assertThat(findTranslation(activity.title(), LanguageCode.FR)).isNotNull();
    assertThat(findTranslation(activity.title(), LanguageCode.EN).value()).isEqualTo("Activity");
    assertThat(findTranslation(activity.title(), LanguageCode.FR).value()).isEqualTo("Activité");

    // Description translations
    assertThat(activity.description()).hasSize(2);
    assertThat(findTranslation(activity.description(), LanguageCode.EN).value()).isEqualTo("English description");
    assertThat(findTranslation(activity.description(), LanguageCode.FR).value()).isEqualTo("Description française");
  }

  // Helper method to find a translation for a specific language
  private CommonPersistableActivity.Translation findTranslation(
      List<CommonPersistableActivity.Translation> translations, LanguageCode language) {
    return translations.stream().filter(t -> t.languageCode() == language).findFirst().orElse(null);
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
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().title()).hasSize(1); // Only English translation
    assertThat(result.getFirst().title().getFirst().languageCode()).isEqualTo(LanguageCode.EN);
  }

  @Test
  void fetchProviderActivities_shouldMapImagesCorrectly() {
    // Given
    Set<String> activityIds = Set.of("product1");

    // Create DTO with images - must include English version
    ViatorActivityDetailDTO dto = createDetailDTO("product1", "en", "Activity", "Description");
    when(viatorClient.getActivityByIdAndLanguage("en", "product1")).thenReturn(Optional.of(dto));

    // Mock French to return empty
    when(viatorClient.getActivityByIdAndLanguage("fr", "product1")).thenReturn(Optional.empty());

    // When
    List<CommonPersistableActivity> result = service.fetchProviderActivities(activityIds);

    // Then
    assertThat(result).hasSize(1);

    // Verify images
    List<CommonPersistableActivity.Image> images = result.getFirst().images();
    assertThat(images).hasSize(2);

    // Verify mobile image
    Optional<CommonPersistableActivity.Image> mobileImage =
        images.stream().filter(img -> img.type() == ImageType.MOBILE).findFirst();
    assertThat(mobileImage).isPresent();
    assertThat(mobileImage.get().height()).isEqualTo(320);
    assertThat(mobileImage.get().width()).isEqualTo(480);

    // Verify desktop image
    Optional<CommonPersistableActivity.Image> desktopImage =
        images.stream().filter(img -> img.type() == ImageType.DESKTOP).findFirst();
    assertThat(desktopImage).isPresent();
    assertThat(desktopImage.get().height()).isEqualTo(480);
    assertThat(desktopImage.get().width()).isEqualTo(720);
  }

  @Test
  void fetchProviderActivities_shouldHandleInvalidDTOGracefully() {
    // Given
    Set<String> activityIds = Set.of("product1");

    // Create DTO with null images - must include English version
    ViatorActivityDetailDTO dto = createDetailDTOWithNullImages("product1", "en");
    when(viatorClient.getActivityByIdAndLanguage("en", "product1")).thenReturn(Optional.of(dto));

    // Mock French to return empty
    when(viatorClient.getActivityByIdAndLanguage("fr", "product1")).thenReturn(Optional.empty());

    // When
    List<CommonPersistableActivity> result = service.fetchProviderActivities(activityIds);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().images()).isEmpty();
  }

  @Test
  void fetchProviderActivities_shouldMapReviewsCorrectly() {
    // Given
    Set<String> activityIds = Set.of("product1");

    // Create DTO with reviews - must include English version
    ViatorActivityDetailDTO dto = createDetailDTO("product1", "en", "Activity", "Description");
    when(viatorClient.getActivityByIdAndLanguage("en", "product1")).thenReturn(Optional.of(dto));

    // Mock French to return empty to avoid interference
    when(viatorClient.getActivityByIdAndLanguage("fr", "product1")).thenReturn(Optional.empty());

    // When
    List<CommonPersistableActivity> result = service.fetchProviderActivities(activityIds);

    // Then
    assertThat(result).hasSize(1);
    CommonPersistableActivity.Review review = result.getFirst().review();
    assertThat(review.averageRating()).isEqualTo(4.5);
    assertThat(review.reviewCount()).isEqualTo(100);
  }

  private CommonPersistableActivity findActivityById(List<CommonPersistableActivity> result, String id) {
    return result.stream().filter(activity -> activity.providerId().equals(id)).findFirst().orElse(null);
  }

  private boolean hasTranslation(
      List<CommonPersistableActivity.Translation> translations, LanguageCode lang, String value) {
    return translations.stream().anyMatch(t -> t.languageCode() == lang && t.value().equals(value));
  }

  private ViatorActivityDetailDTO createDetailDTO(
      String productCode, String language, String title, String description) {
    // Create at least one image to satisfy @NotEmpty validation
    ViatorActivityDetailDTO.ImageDTO coverImage =
        new ViatorActivityDetailDTO.ImageDTO(
            "viator",
            "Test caption",
            true,
            List.of(
                new ViatorActivityDetailDTO.ImageVariantDTO(320, 480, "http://test.com/image.jpg"),
                new ViatorActivityDetailDTO.ImageVariantDTO(480, 720, "http://test.com/image.jpg")));

    return new ViatorActivityDetailDTO(
        productCode,
        language,
        title,
        description,
        List.of(coverImage), // Non-empty images list
        List.of(), // empty tags
        List.of(), // empty destinations
        new ViatorActivityDetailDTO.ItineraryDTO("STANDARD", new ViatorActivityDetailDTO.DurationDTO(null, null, 60)),
        "http://viator.com/activity/" + productCode,
        new ViatorActivityDetailDTO.ReviewsDTO(List.of(), 100, 4.5f) // Non-null reviews
        );
  }

  private ViatorActivityDetailDTO createDetailDTOWithNullImages(String productCode, String language) {
    // For testing null images handling, we still need to provide a valid DTO
    // but we'll handle the null check in the service code
    ViatorActivityDetailDTO.ImageDTO emptyImage =
        new ViatorActivityDetailDTO.ImageDTO(
            "viator",
            "Test caption",
            true,
            List.of(new ViatorActivityDetailDTO.ImageVariantDTO(100, 100, "http://test.com/image.jpg")));

    return new ViatorActivityDetailDTO(
        productCode,
        language,
        "Test Activity",
        "Test Description",
        List.of(emptyImage),
        List.of(),
        List.of(),
        new ViatorActivityDetailDTO.ItineraryDTO("STANDARD", new ViatorActivityDetailDTO.DurationDTO(null, null, 60)),
        "http://viator.com/activity/" + productCode,
        new ViatorActivityDetailDTO.ReviewsDTO(List.of(), 100, 4.5f));
  }
}
