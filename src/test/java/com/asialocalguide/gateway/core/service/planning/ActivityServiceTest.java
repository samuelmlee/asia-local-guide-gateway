package com.asialocalguide.gateway.core.service.planning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.Language;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.planning.Activity;
import com.asialocalguide.gateway.core.domain.planning.CommonPersistableActivity;
import com.asialocalguide.gateway.core.exception.ActivityCachingException;
import com.asialocalguide.gateway.core.repository.ActivityRepository;
import com.asialocalguide.gateway.core.service.LanguageService;
import com.asialocalguide.gateway.core.service.bookingprovider.BookingProviderService;
import com.asialocalguide.gateway.core.service.strategy.FetchActivityStrategy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

  @Mock private ActivityRepository activityRepository;
  @Mock private BookingProviderService bookingProviderService;
  @Mock private LanguageService languageService;
  @Mock private FetchActivityStrategy mockStrategy;

  @InjectMocks private ActivityService service;

  @Captor ArgumentCaptor<List<Activity>> activitiesCaptor;
  private BookingProvider viatorProvider;
  private Set<String> validActivityIds;
  private CommonPersistableActivity persistableActivity;

  @BeforeEach
  void setup() {
    viatorProvider = new BookingProvider(BookingProviderName.VIATOR);
    validActivityIds = Set.of("activity1", "activity2");

    CommonPersistableActivity.Review review = new CommonPersistableActivity.Review(4.5f, 100);
    CommonPersistableActivity.Translation enTitle =
        new CommonPersistableActivity.Translation(LanguageCode.EN, "Activity Title");
    CommonPersistableActivity.Translation enDesc =
        new CommonPersistableActivity.Translation(LanguageCode.EN, "Activity Description");

    persistableActivity =
        new CommonPersistableActivity(
            List.of(enTitle),
            List.of(enDesc),
            List.of(),
            review,
            60,
            "https://example.com",
            BookingProviderName.VIATOR,
            "activity1");
  }

  @Test
  void findExistingIdsByProviderNameAndIds_shouldReturnIdsFromRepository() {
    when(activityRepository.findExistingIdsByProviderNameAndIds(any(), any())).thenReturn(validActivityIds);

    Set<String> result = service.findExistingIdsByProviderNameAndIds(BookingProviderName.VIATOR, validActivityIds);

    assertThat(result).isEqualTo(validActivityIds);
    verify(activityRepository).findExistingIdsByProviderNameAndIds(BookingProviderName.VIATOR, validActivityIds);
  }

  @Test
  void findExistingIdsByProviderNameAndIds_shouldThrowWhenProviderNameIsNull() {
    assertThatThrownBy(() -> service.findExistingIdsByProviderNameAndIds(null, validActivityIds))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void findExistingIdsByProviderNameAndIds_shouldThrowWhenActivityIdsIsNull() {
    assertThatThrownBy(() -> service.findExistingIdsByProviderNameAndIds(BookingProviderName.VIATOR, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void findExistingIdsByProviderNameAndIds_shouldThrowWhenActivityIdsIsEmpty() {
    assertThatThrownBy(() -> service.findExistingIdsByProviderNameAndIds(BookingProviderName.VIATOR, Set.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void findActivitiesByProviderNameAndIds_shouldReturnActivitiesFromRepository() {
    Set<Activity> expectedActivities = Set.of(new Activity());
    when(activityRepository.findActivitiesByProviderNameAndIds(any(), any())).thenReturn(expectedActivities);

    Set<Activity> result = service.findActivitiesByProviderNameAndIds(BookingProviderName.VIATOR, validActivityIds);

    assertThat(result).isEqualTo(expectedActivities);
    verify(activityRepository).findActivitiesByProviderNameAndIds(BookingProviderName.VIATOR, validActivityIds);
  }

  @Test
  void findActivitiesByProviderNameAndIds_shouldThrowWhenProviderNameIsNull() {
    assertThatThrownBy(() -> service.findActivitiesByProviderNameAndIds(null, validActivityIds))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void findActivitiesByProviderNameAndIds_shouldThrowWhenActivityIdsIsNull() {
    assertThatThrownBy(() -> service.findActivitiesByProviderNameAndIds(BookingProviderName.VIATOR, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void findActivitiesByProviderNameAndIds_shouldThrowWhenActivityIdsIsEmpty() {
    assertThatThrownBy(() -> service.findActivitiesByProviderNameAndIds(BookingProviderName.VIATOR, Set.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void saveAll_shouldReturnSavedActivitiesFromRepository() {
    List<Activity> activitiesToSave = List.of(new Activity());
    when(activityRepository.saveAll(any())).thenReturn(activitiesToSave);

    List<Activity> result = service.saveAll(activitiesToSave);

    assertThat(result).isEqualTo(activitiesToSave);
    verify(activityRepository).saveAll(activitiesToSave);
  }

  @Test
  void saveAll_shouldThrowWhenActivitiesIsNull() {
    assertThatThrownBy(() -> service.saveAll(null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void saveAll_shouldThrowWhenActivitiesIsEmpty() {
    assertThatThrownBy(() -> service.saveAll(List.of())).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void cacheNewActivitiesByProvider_shouldFetchAndSaveNewActivities() {
    // Setup
    Map<BookingProviderName, Set<String>> providerToIds =
        Map.of(BookingProviderName.VIATOR, Set.of("activity1", "activity2"));

    when(bookingProviderService.getAllBookingProviders()).thenReturn(List.of(viatorProvider));
    when(mockStrategy.getProviderName()).thenReturn(BookingProviderName.VIATOR);
    when(mockStrategy.fetchProviderActivities(any())).thenReturn(List.of(persistableActivity));
    when(activityRepository.findExistingIdsByProviderNameAndIds(any(), any())).thenReturn(Set.of("activity2"));

    // Service has only one strategy
    service = new ActivityService(activityRepository, bookingProviderService, languageService, List.of(mockStrategy));

    // Execute
    service.cacheNewActivitiesByProvider(providerToIds);

    // Verify repository was called with transformed activities
    activitiesCaptor = ArgumentCaptor.forClass(List.class);
    verify(activityRepository).saveAll(activitiesCaptor.capture());

    List<Activity> savedActivities = activitiesCaptor.getValue();
    assertThat(savedActivities).hasSize(1);
    assertThat(savedActivities.get(0).getProviderActivityId()).isEqualTo("activity1");
  }

  @Test
  void toActivity_shouldAddTranslationsSuccessfully() {
    // Setup
    when(languageService.getAllLanguages())
        .thenReturn(List.of(new Language(1L, LanguageCode.EN), new Language(2L, LanguageCode.FR)));

    CommonPersistableActivity.Translation enTitle =
        new CommonPersistableActivity.Translation(LanguageCode.EN, "English Title");
    CommonPersistableActivity.Translation frTitle =
        new CommonPersistableActivity.Translation(LanguageCode.FR, "French Title");
    CommonPersistableActivity.Translation enDesc =
        new CommonPersistableActivity.Translation(LanguageCode.EN, "English Description");
    CommonPersistableActivity.Translation frDesc =
        new CommonPersistableActivity.Translation(LanguageCode.FR, "French Description");

    CommonPersistableActivity activity =
        new CommonPersistableActivity(
            List.of(enTitle, frTitle),
            List.of(enDesc, frDesc),
            List.of(),
            new CommonPersistableActivity.Review(4.5f, 100),
            60,
            "https://example.com",
            BookingProviderName.VIATOR,
            "activity1");

    Map<BookingProviderName, Set<String>> providerToIds = Map.of(BookingProviderName.VIATOR, Set.of("activity1"));
    when(bookingProviderService.getAllBookingProviders()).thenReturn(List.of(viatorProvider));
    when(mockStrategy.getProviderName()).thenReturn(BookingProviderName.VIATOR);
    when(mockStrategy.fetchProviderActivities(any())).thenReturn(List.of(activity));

    // Execute
    service = new ActivityService(activityRepository, bookingProviderService, languageService, List.of(mockStrategy));
    service.cacheNewActivitiesByProvider(providerToIds);

    // Verify
    activitiesCaptor = ArgumentCaptor.forClass(List.class);
    verify(activityRepository).saveAll(activitiesCaptor.capture());

    Activity savedActivity = activitiesCaptor.getValue().get(0);
    assertThat(savedActivity.getActivityTranslations()).hasSize(2);
  }

  @Test
  void toActivity_shouldSkipTranslationsForMissingLanguages() {
    // Setup - only provide English language, but include titles for EN and FR
    when(languageService.getAllLanguages()).thenReturn(List.of(new Language(1L, LanguageCode.EN)));

    CommonPersistableActivity.Translation enTitle =
        new CommonPersistableActivity.Translation(LanguageCode.EN, "English Title");
    CommonPersistableActivity.Translation frTitle =
        new CommonPersistableActivity.Translation(LanguageCode.FR, "French Title");

    CommonPersistableActivity activity =
        new CommonPersistableActivity(
            List.of(enTitle, frTitle),
            List.of(),
            List.of(),
            new CommonPersistableActivity.Review(4.5f, 100),
            60,
            "https://example.com",
            BookingProviderName.VIATOR,
            "activity1");

    Map<BookingProviderName, Set<String>> providerToIds = Map.of(BookingProviderName.VIATOR, Set.of("activity1"));
    when(bookingProviderService.getAllBookingProviders()).thenReturn(List.of(viatorProvider));
    when(mockStrategy.getProviderName()).thenReturn(BookingProviderName.VIATOR);
    when(mockStrategy.fetchProviderActivities(any())).thenReturn(List.of(activity));
    when(activityRepository.findExistingIdsByProviderNameAndIds(any(), any())).thenReturn(Set.of());

    // Execute
    service = new ActivityService(activityRepository, bookingProviderService, languageService, List.of(mockStrategy));
    service.cacheNewActivitiesByProvider(providerToIds);

    // Verify only English translation was added
    activitiesCaptor = ArgumentCaptor.forClass(List.class);
    verify(activityRepository).saveAll(activitiesCaptor.capture());

    Activity savedActivity = activitiesCaptor.getValue().get(0);
    assertThat(savedActivity.getActivityTranslations()).hasSize(1);
    assertThat(savedActivity.getActivityTranslations().iterator().next().getLanguage().getCode())
        .isEqualTo(LanguageCode.EN);
  }

  @Test
  void cacheNewActivitiesByProvider_shouldThrowWhenProviderNameToIdIsNull() {
    assertThatThrownBy(() -> service.cacheNewActivitiesByProvider(null)).isInstanceOf(ActivityCachingException.class);
  }

  @Test
  void cacheNewActivitiesByProvider_shouldThrowWhenProviderNameToIdIsEmpty() {
    assertThatThrownBy(() -> service.cacheNewActivitiesByProvider(Map.of()))
        .isInstanceOf(ActivityCachingException.class);
  }

  @Test
  void cacheNewActivitiesByProvider_shouldHandleAllActivitiesAlreadyCached() {
    // Setup
    Map<BookingProviderName, Set<String>> providerToIds =
        Map.of(BookingProviderName.VIATOR, Set.of("activity1", "activity2"));

    when(bookingProviderService.getAllBookingProviders()).thenReturn(List.of(viatorProvider));
    // All IDs already exist in DB
    when(activityRepository.findExistingIdsByProviderNameAndIds(any(), any()))
        .thenReturn(Set.of("activity1", "activity2"));
    when(mockStrategy.getProviderName()).thenReturn(BookingProviderName.VIATOR);

    service = new ActivityService(activityRepository, bookingProviderService, languageService, List.of(mockStrategy));

    // Execute
    service.cacheNewActivitiesByProvider(providerToIds);

    // Verify no activities were saved since all existed
    verify(mockStrategy, never()).fetchProviderActivities(any());
    verify(activityRepository, never()).saveAll(any());
  }

  @Test
  void cacheNewActivitiesByProvider_shouldHandleNoMatchingStrategies() {
    // Setup
    Map<BookingProviderName, Set<String>> providerToIds = Map.of(BookingProviderName.VIATOR, Set.of("activity1"));

    // No matching strategy (returns different provider name)
    when(mockStrategy.getProviderName()).thenReturn(BookingProviderName.GET_YOUR_GUIDE);

    service = new ActivityService(activityRepository, bookingProviderService, languageService, List.of(mockStrategy));

    // Execute
    service.cacheNewActivitiesByProvider(providerToIds);

    // Verify no activities were saved
    verify(activityRepository, never()).saveAll(any());
  }

  @Test
  void cacheNewActivitiesByProvider_shouldHandleNoBookingProvidersFound() {
    // Setup
    Map<BookingProviderName, Set<String>> providerToIds = Map.of(BookingProviderName.VIATOR, Set.of("activity1"));

    when(mockStrategy.getProviderName()).thenReturn(BookingProviderName.VIATOR);
    when(bookingProviderService.getAllBookingProviders()).thenReturn(List.of());

    service = new ActivityService(activityRepository, bookingProviderService, languageService, List.of(mockStrategy));

    // Execute
    service.cacheNewActivitiesByProvider(providerToIds);

    // Verify no activities were saved
    verify(activityRepository, never()).saveAll(any());
  }

  @Test
  void cacheNewActivitiesByProvider_shouldHandleStrategyExceptions() {
    // Setup
    Map<BookingProviderName, Set<String>> providerToIds = Map.of(BookingProviderName.VIATOR, Set.of("activity1"));

    when(bookingProviderService.getAllBookingProviders()).thenReturn(List.of(viatorProvider));
    when(activityRepository.findExistingIdsByProviderNameAndIds(any(), any())).thenReturn(Set.of());
    when(mockStrategy.getProviderName()).thenReturn(BookingProviderName.VIATOR);
    when(mockStrategy.fetchProviderActivities(any())).thenThrow(new RuntimeException("Test exception"));

    service = new ActivityService(activityRepository, bookingProviderService, languageService, List.of(mockStrategy));

    // Execute - should not throw exception
    service.cacheNewActivitiesByProvider(providerToIds);

    // Verify no activities were saved
    verify(activityRepository, never()).saveAll(any());
  }

  @Test
  void cacheNewActivitiesByProvider_shouldHandleRepositoryExceptions() {
    // Setup
    Map<BookingProviderName, Set<String>> providerToIds = Map.of(BookingProviderName.VIATOR, Set.of("activity1"));

    when(bookingProviderService.getAllBookingProviders()).thenReturn(List.of(viatorProvider));
    when(activityRepository.findExistingIdsByProviderNameAndIds(any(), any())).thenReturn(Set.of());
    when(mockStrategy.getProviderName()).thenReturn(BookingProviderName.VIATOR);
    when(mockStrategy.fetchProviderActivities(any())).thenReturn(List.of(persistableActivity));
    when(activityRepository.saveAll(any())).thenThrow(new RuntimeException("Database error"));

    service = new ActivityService(activityRepository, bookingProviderService, languageService, List.of(mockStrategy));

    // Execute - method should not propagate the exception
    assertThatThrownBy(() -> service.cacheNewActivitiesByProvider(providerToIds))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Database error");
  }
}
