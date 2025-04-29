package com.asialocalguide.gateway.core.service.planning;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.planning.*;
import com.asialocalguide.gateway.core.domain.user.AuthProviderName;
import com.asialocalguide.gateway.core.domain.user.User;
import com.asialocalguide.gateway.core.dto.planning.DayActivityDTO;
import com.asialocalguide.gateway.core.dto.planning.DayPlanDTO;
import com.asialocalguide.gateway.core.dto.planning.PlanningCreateRequestDTO;
import com.asialocalguide.gateway.core.dto.planning.PlanningRequestDTO;
import com.asialocalguide.gateway.core.exception.UserNotFoundException;
import com.asialocalguide.gateway.core.repository.PlanningRepository;
import com.asialocalguide.gateway.core.service.strategy.FetchPlanningDataStrategy;
import com.asialocalguide.gateway.core.service.user.UserService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PlanningService {

  private final List<FetchPlanningDataStrategy> fetchPlanningDataStrategies;

  private final UserService userService;

  private final ActivityService activityService;

  private final PlanningRepository planningRepository;

  public PlanningService(
      List<FetchPlanningDataStrategy> fetchPlanningDataStrategies,
      UserService userService,
      ActivityService activityService,
      PlanningRepository planningRepository) {
    this.fetchPlanningDataStrategies = fetchPlanningDataStrategies;
    this.userService = userService;
    this.activityService = activityService;
    this.planningRepository = planningRepository;
  }

  public List<DayPlanDTO> generateActivityPlanning(PlanningRequestDTO request) {

    Locale locale = LocaleContextHolder.getLocale();

    LanguageCode languageCode = LanguageCode.from(locale.getLanguage()).orElse(LanguageCode.EN);

    List<ProviderPlanningData> providerDataList =
        fetchPlanningDataStrategies.stream()
            .map(
                strategy -> {
                  try {
                    return strategy.fetchProviderPlanningData(request, languageCode);
                  } catch (Exception e) {
                    log.error("Error during fetching of activities from Provider : {}", strategy.getProviderName(), e);
                    return null;
                  }
                })
            .filter(Objects::nonNull)
            .toList();

    if (providerDataList.isEmpty()) {
      return List.of();
    }

    // Implement merging of ProviderActivityData when using multiple providers
    ProviderPlanningData result = providerDataList.getFirst();

    // Generate availability 3d array using scheduler
    boolean[][][] schedule = ActivitySchedulerWithRatings.scheduleActivities(result.activityPlanningData());

    return createDayPlans(
        request.startDate(),
        request.getDuration(),
        result.activities(),
        schedule,
        result.activityPlanningData().getValidStartTimes());
  }

  private List<DayPlanDTO> createDayPlans(
      LocalDate startDate,
      long totalDays,
      List<CommonActivity> activities,
      boolean[][][] schedule,
      String[][][] startTimes) {

    List<DayPlanDTO> dayPlans = new ArrayList<>();

    for (int dayIndex = 0; dayIndex < totalDays; dayIndex++) {
      LocalDate currentDay = startDate.plusDays(dayIndex);

      List<DayActivityDTO> dayActivities =
          assignActivitiesForDay(activities, schedule, startTimes, dayIndex, currentDay);

      dayPlans.add(new DayPlanDTO(currentDay, dayActivities));
    }

    return dayPlans;
  }

  private List<DayActivityDTO> assignActivitiesForDay(
      List<CommonActivity> activities, boolean[][][] schedule, String[][][] startTimes, int dayIndex, LocalDate day) {

    List<DayActivityDTO> dayActivities = new ArrayList<>();

    for (int a = 0; a < schedule.length; a++) { // Iterate over activities
      for (int t = 0; t < schedule[a][dayIndex].length; t++) { // Iterate over timeslots
        if (schedule[a][dayIndex][t]) { // If activity is scheduled in this timeslot
          String startHour = startTimes[a][dayIndex][t];

          dayActivities.add(createDayActivity(activities.get(a), day, startHour));
        }
      }
    }
    return dayActivities;
  }

  private DayActivityDTO createDayActivity(CommonActivity activity, LocalDate day, String startHour) {

    LocalDateTime startTime = toLocalDateTime(day, startHour);
    LocalDateTime endTime = startTime.plus(Duration.ofMinutes(activity.duration().maxMinutes()));

    return new DayActivityDTO(
        activity.providerId(),
        activity.title(),
        activity.description(),
        activity.reviews().averageRating(),
        activity.reviews().totalReviews(),
        activity.duration().maxMinutes(),
        activity.pricing().amount(),
        activity.pricing().currency(),
        activity.images(),
        activity.bookingUrl(),
        startTime,
        endTime,
        activity.providerName());
  }

  private LocalDateTime toLocalDateTime(LocalDate date, String timeStr) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    LocalTime time = LocalTime.parse(timeStr, formatter);
    return date.atTime(time);
  }

  @Transactional
  public Planning savePlanning(
      PlanningCreateRequestDTO planningRequest, AuthProviderName authProviderName, String userProviderId) {

    User user =
        userService
            .getUserByProviderNameAndProviderUserId(authProviderName, userProviderId)
            .orElseThrow(() -> new UserNotFoundException("User not found for Planning Creation request"));

    List<PlanningCreateRequestDTO.CreateDayActivityDTO> activities =
        planningRequest.dayPlans().stream().flatMap(dayPlan -> dayPlan.activities().stream()).toList();

    Map<BookingProviderName, Set<String>> providerNameToIds =
        activities.stream()
            .collect(
                Collectors.groupingBy(
                    PlanningCreateRequestDTO.CreateDayActivityDTO::bookingProviderName,
                    Collectors.mapping(
                        PlanningCreateRequestDTO.CreateDayActivityDTO::productCode, Collectors.toSet())));

    activityService.persistNewActivitiesByProvider(providerNameToIds);

    Map<BookingProviderName, Map<String, Activity>> activityLookupMap = buildActivityLookupMap(providerNameToIds);

    Planning planning = new Planning(user, planningRequest.name());

    planningRequest
        .dayPlans()
        .forEach(
            dayPlanDTO -> {
              DayPlan dayPlan = new DayPlan(dayPlanDTO.date());

              List<PlanningCreateRequestDTO.CreateDayActivityDTO> dayActivitiesDTO = dayPlanDTO.activities();

              Set<DayActivity> dayActivities = buildDayActivities(dayActivitiesDTO, activityLookupMap);

              dayActivities.forEach(dayPlan::addDayActivity);

              planning.addDayPlan(dayPlan);
            });

    return planningRepository.save(planning);
  }

  private Map<BookingProviderName, Map<String, Activity>> buildActivityLookupMap(
      Map<BookingProviderName, Set<String>> providerNameToIds) {
    Map<BookingProviderName, Map<String, Activity>> result = new EnumMap<>(BookingProviderName.class);

    providerNameToIds.forEach(
        (providerName, activityIds) -> {
          Set<Activity> activities = activityService.findActivitiesByProviderNameAndIds(providerName, activityIds);
          Map<String, Activity> providerActivities =
              activities.stream()
                  .collect(
                      Collectors.toMap(activity -> activity.getId().getProviderActivityId(), activity -> activity));
          result.put(providerName, providerActivities);
        });

    return result;
  }

  private Set<DayActivity> buildDayActivities(
      List<PlanningCreateRequestDTO.CreateDayActivityDTO> dayActivitiesDTO,
      Map<BookingProviderName, Map<String, Activity>> activityLookupMap) {

    if (dayActivitiesDTO == null || activityLookupMap == null) {
      log.warn("Received null input parameters in buildDayActivities");
      return Collections.emptySet();
    }

    Set<DayActivity> dayActivities = new HashSet<>();

    for (PlanningCreateRequestDTO.CreateDayActivityDTO activityDTO : dayActivitiesDTO) {

      if (activityDTO == null) {
        log.warn("Skipping null activity DTO");
        continue;
      }

      BookingProviderName providerName = activityDTO.bookingProviderName();
      String activityId = activityDTO.productCode();

      if (providerName == null || activityId == null || activityId.isBlank()) {
        log.warn("Skipping activity with invalid provider or ID: provider={}, id={}", providerName, activityId);
        continue;
      }

      Map<String, Activity> providerActivities = activityLookupMap.getOrDefault(providerName, Map.of());
      Activity activity = providerActivities.get(activityId);

      if (activity == null) {
        log.warn("Activity not found for provider: {} and ID: {}", providerName, activityId);
        continue;
      }

      LocalDateTime startTime = activityDTO.startTime();
      LocalDateTime endTime = activityDTO.endTime();

      if (startTime == null || endTime == null) {
        log.warn("Invalid time range for activity: provider={}, id={}", providerName, activityId);
        continue;
      }

      DayActivity dayActivity = new DayActivity(activity, startTime, endTime);
      dayActivities.add(dayActivity);
    }

    return dayActivities;
  }
}
