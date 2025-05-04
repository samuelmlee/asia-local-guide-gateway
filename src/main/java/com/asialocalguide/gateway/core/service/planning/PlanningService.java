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
import com.asialocalguide.gateway.core.exception.PlanningCreationException;
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
import java.util.function.Function;
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

    validateSavePlanningInput(planningRequest, authProviderName, userProviderId);

    Map<BookingProviderName, Set<String>> providerNameToIds = buildProviderNameToActivityIds(planningRequest);

    User user = getUserForPlanning(planningRequest, authProviderName, userProviderId);

    persistNewActivitiesForPlanning(providerNameToIds);

    Map<BookingProviderName, Map<String, Activity>> activityLookupMap = buildActivityLookupMap(providerNameToIds);

    if (activityLookupMap.isEmpty()) {
      throw new PlanningCreationException("Error fetching any activities for the provided planning request.");
    }

    Planning planning = buildPlanningEntity(planningRequest, user, activityLookupMap);

    if (planning.getDayPlans() == null || planning.getDayPlans().isEmpty()) {
      throw new PlanningCreationException("Error fetching any activities for the planning request and day plans.");
    }

    return persistPlanning(planning);
  }

  private void validateSavePlanningInput(
      PlanningCreateRequestDTO planningRequest, AuthProviderName authProviderName, String userProviderId) {

    if (planningRequest == null || authProviderName == null || userProviderId == null) {
      throw new PlanningCreationException(
          "PlanningCreateRequestDTO or AuthProviderName or userProviderId cannot be null");
    }

    if (planningRequest.name() == null || planningRequest.name().isBlank()) {
      throw new PlanningCreationException("Planning name cannot be null or empty");
    }

    if (planningRequest.dayPlans() == null || planningRequest.dayPlans().isEmpty()) {
      throw new PlanningCreationException("Day plans cannot be null or empty");
    }

    for (PlanningCreateRequestDTO.CreateDayPlanDTO dayPlan : planningRequest.dayPlans()) {
      if (dayPlan.activities() == null || dayPlan.activities().isEmpty()) {
        throw new PlanningCreationException("Day plan activities cannot be null or empty");
      }
    }
  }

  private User getUserForPlanning(
      PlanningCreateRequestDTO planningRequest, AuthProviderName authProviderName, String userProviderId) {
    return userService
        .getUserByProviderNameAndProviderUserId(authProviderName, userProviderId)
        .orElseThrow(
            () ->
                new UserNotFoundException(
                    String.format(
                        "User not found for Planning Creation request: %s, AuthProviderName: %s, userProviderId:"
                            + " %s",
                        planningRequest, authProviderName, userProviderId)));
  }

  private static Map<BookingProviderName, Set<String>> buildProviderNameToActivityIds(
      PlanningCreateRequestDTO planningRequest) {
    List<PlanningCreateRequestDTO.CreateDayActivityDTO> activities =
        planningRequest.dayPlans().stream().flatMap(dayPlan -> dayPlan.activities().stream()).toList();

    if (activities.isEmpty()) {
      throw new PlanningCreationException("No activities found in the planning request");
    }

    return activities.stream()
        .collect(
            Collectors.groupingBy(
                PlanningCreateRequestDTO.CreateDayActivityDTO::bookingProviderName,
                Collectors.mapping(PlanningCreateRequestDTO.CreateDayActivityDTO::productCode, Collectors.toSet())));
  }

  private Map<BookingProviderName, Map<String, Activity>> buildActivityLookupMap(
      Map<BookingProviderName, Set<String>> providerNameToIds) {
    Map<BookingProviderName, Map<String, Activity>> result = new EnumMap<>(BookingProviderName.class);

    providerNameToIds.forEach(
        (providerName, activityIds) -> {
          try {
            Set<Activity> activities = activityService.findActivitiesByProviderNameAndIds(providerName, activityIds);
            Map<String, Activity> providerActivities =
                activities.stream().collect(Collectors.toMap(Activity::getProviderActivityId, Function.identity()));
            result.put(providerName, providerActivities);

          } catch (Exception ex) {
            log.error(
                "Error during fetching activities for provider: {}, activityIds: {}", providerName, activityIds, ex);
          }
        });

    return result;
  }

  private void persistNewActivitiesForPlanning(Map<BookingProviderName, Set<String>> providerNameToIds) {
    try {
      activityService.cacheNewActivitiesByProvider(providerNameToIds);
    } catch (Exception ex) {
      throw new PlanningCreationException("Error during persisting new activities for Planning creation", ex);
    }
  }

  private Planning buildPlanningEntity(
      PlanningCreateRequestDTO planningRequest,
      User user,
      Map<BookingProviderName, Map<String, Activity>> activityLookupMap) {
    Planning planning = new Planning(user, planningRequest.name());

    planningRequest
        .dayPlans()
        .forEach(
            dayPlanDTO -> {
              List<PlanningCreateRequestDTO.CreateDayActivityDTO> dayActivitiesDTO = dayPlanDTO.activities();

              // Not persisting empty day plans
              if (dayActivitiesDTO == null || dayActivitiesDTO.isEmpty()) {
                log.info("Skipping empty day plan for dayPlan: {}", dayPlanDTO);
                return;
              }

              Optional<DayPlan> dayPlanOpt = buildDayPlanEntity(dayPlanDTO, dayActivitiesDTO, activityLookupMap);

              dayPlanOpt.ifPresent(planning::addDayPlan);
            });
    return planning;
  }

  private Optional<DayPlan> buildDayPlanEntity(
      PlanningCreateRequestDTO.CreateDayPlanDTO dayPlanDTO,
      List<PlanningCreateRequestDTO.CreateDayActivityDTO> dayActivitiesDTO,
      Map<BookingProviderName, Map<String, Activity>> activityLookupMap) {
    DayPlan dayPlan = new DayPlan(dayPlanDTO.date());

    Set<DayActivity> dayActivities = buildDayActivityEntities(dayActivitiesDTO, activityLookupMap);

    if (dayActivities.isEmpty()) {
      log.info("No valid activities found for DayPlan: {}", dayPlanDTO);
      return Optional.empty();
    }

    dayActivities.forEach(dayPlan::addDayActivity);
    return Optional.of(dayPlan);
  }

  private Set<DayActivity> buildDayActivityEntities(
      List<PlanningCreateRequestDTO.CreateDayActivityDTO> dayActivitiesDTO,
      Map<BookingProviderName, Map<String, Activity>> activityLookupMap) {

    if (dayActivitiesDTO == null || activityLookupMap == null) {
      log.warn("Received null input parameters in buildDayActivities");
      return Collections.emptySet();
    }

    Set<DayActivity> dayActivities = new HashSet<>();

    for (PlanningCreateRequestDTO.CreateDayActivityDTO activityDTO : dayActivitiesDTO) {

      if (!validateActivity(activityDTO)) {
        log.warn("Skipping invalid activity data: {}", activityDTO);
        continue;
      }

      BookingProviderName providerName = activityDTO.bookingProviderName();
      String activityId = activityDTO.productCode();

      Map<String, Activity> providerActivities = activityLookupMap.getOrDefault(providerName, Map.of());
      Activity activity = providerActivities.get(activityId);

      if (activity == null) {
        log.warn("Activity not found for provider: {}, activityId: {}", providerName, activityId);
        continue;
      }

      LocalDateTime startTime = activityDTO.startTime();
      LocalDateTime endTime = activityDTO.endTime();

      DayActivity dayActivity = new DayActivity(activity, startTime, endTime);
      dayActivities.add(dayActivity);
    }

    return dayActivities;
  }

  private boolean validateActivity(PlanningCreateRequestDTO.CreateDayActivityDTO activity) {
    if (activity == null) {
      log.warn("Activity is null");
      return false;
    }

    if (activity.bookingProviderName() == null) {
      log.warn("Activity booking provider name is null");
      return false;
    }

    if (activity.productCode() == null || activity.productCode().isBlank()) {
      log.warn("Activity product code is null or blank");
      return false;
    }

    if (activity.startTime() == null || activity.endTime() == null) {
      log.warn("Activity start or end time is null for {}", activity.productCode());
      return false;
    }

    if (activity.startTime().isAfter(activity.endTime())) {
      log.warn("Activity start time is after end time for {}", activity.productCode());
      return false;
    }

    return true;
  }

  private Planning persistPlanning(Planning planning) {
    try {
      return planningRepository.save(planning);
    } catch (Exception ex) {
      throw new PlanningCreationException("Error when persisting planning", ex);
    }
  }
}
