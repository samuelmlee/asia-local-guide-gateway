package com.asialocalguide.gateway.core.service.planning;

import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity;
import com.asialocalguide.gateway.core.domain.planning.Planning;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningData;
import com.asialocalguide.gateway.core.dto.planning.DayActivityDTO;
import com.asialocalguide.gateway.core.dto.planning.DayPlanDTO;
import com.asialocalguide.gateway.core.dto.planning.PlanningCreateRequestDTO;
import com.asialocalguide.gateway.core.dto.planning.PlanningRequestDTO;
import com.asialocalguide.gateway.core.service.strategy.FetchPlanningDataStrategy;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PlanningService {

  List<FetchPlanningDataStrategy> fetchPlanningDataStrategies;

  public PlanningService(List<FetchPlanningDataStrategy> fetchPlanningDataStrategies) {
    this.fetchPlanningDataStrategies = fetchPlanningDataStrategies;
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

  public Planning savePlanning(PlanningCreateRequestDTO planningRequest) {

    return new Planning();
  }
}
