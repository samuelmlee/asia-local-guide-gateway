package com.asialocalguide.gateway.core.service.planning;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.domain.planning.ActivityData;
import com.asialocalguide.gateway.core.dto.planning.ActivityPlanningRequestDTO;
import com.asialocalguide.gateway.core.dto.planning.DayActivityDTO;
import com.asialocalguide.gateway.core.dto.planning.DayPlanDTO;
import com.asialocalguide.gateway.core.service.ViatorActivityAvailabilityMapper;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDetailDTO;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PlanningService {

  private final ActivityService activityService;

  public PlanningService(ActivityService activityService) {
    this.activityService = activityService;
  }

  public List<DayPlanDTO> generateActivityPlanning(ActivityPlanningRequestDTO request) {
    SupportedLocale locale = SupportedLocale.getDefaultLocale();
    List<ViatorActivityDetailDTO> activityDetails = activityService.getActivities(locale, request);

    // Extract the list of ViatorActivityDTO
    List<ViatorActivityDTO> activities = activityDetails.stream().map(ViatorActivityDetailDTO::activity).toList();

    LocalDate startDate = request.startDate();
    LocalDate endDate = request.endDate();
    long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

    ActivityData activityData = ViatorActivityAvailabilityMapper.mapToActivityData(activityDetails, startDate, endDate);

    // Generate availability 3d array using scheduler
    boolean[][][] schedule = ActivitySchedulerWithRatings.scheduleActivities(activityData);

    return createDayPlans(startDate, totalDays, activities, schedule, activityData.getValidStartTimes());
  }

  private List<DayPlanDTO> createDayPlans(
      LocalDate startDate,
      long totalDays,
      List<ViatorActivityDTO> activities,
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
      List<ViatorActivityDTO> activities,
      boolean[][][] schedule,
      String[][][] startTimes,
      int dayIndex,
      LocalDate day) {

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

  private DayActivityDTO createDayActivity(ViatorActivityDTO activity, LocalDate day, String startHour) {

    LocalDateTime startTime = toLocalDateTime(day, startHour);
    LocalDateTime endTime = startTime.plus(getDuration(activity));

    return new DayActivityDTO(
        activity.productCode(),
        activity.title(),
        activity.description(),
        activity.reviews().combinedAverageRating(),
        activity.reviews().totalReviews(),
        activity.getDurationMinutes(),
        activity.pricing().summary().fromPrice(),
        activity.images(),
        activity.productUrl(),
        startTime,
        endTime);
  }

  private LocalDateTime toLocalDateTime(LocalDate date, String timeStr) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    LocalTime time = LocalTime.parse(timeStr, formatter);
    return date.atTime(time);
  }

  private Duration getDuration(ViatorActivityDTO activity) {
    return activity.getDurationMinutes() == 0 ? Duration.ZERO : Duration.ofMinutes(activity.getDurationMinutes());
  }
}
