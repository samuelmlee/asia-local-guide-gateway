package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.domain.AvailabilityResult;
import com.asialocalguide.gateway.core.dto.ActivityPlanningRequestDTO;
import com.asialocalguide.gateway.core.dto.DayActivityDTO;
import com.asialocalguide.gateway.core.dto.DayPlanDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityAvailabilityDTO;
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
  private final ActivitySchedulerWithRatings scheduler;

  public PlanningService(ActivityService activityService, ActivitySchedulerWithRatings scheduler) {
    this.activityService = activityService;
    this.scheduler = scheduler;
  }

  public List<DayPlanDTO> generateActivityPlanning(ActivityPlanningRequestDTO request) {
    SupportedLocale locale = SupportedLocale.getDefaultLocale();
    List<ViatorActivityDetailDTO> activityDetails = activityService.getActivities(locale, request);

    // Extract the list of ViatorActivityDTO
    List<ViatorActivityDTO> activities =
        activityDetails.stream().map(ViatorActivityDetailDTO::activity).toList();
    // Extract the list of ViatorActivityAvailabilityDTO
    List<ViatorActivityAvailabilityDTO> availabilities =
        activityDetails.stream().map(ViatorActivityDetailDTO::availability).toList();

    // Convert request date range to local dates
    LocalDate startDate = request.startDate();
    LocalDate endDate = request.endDate();
    long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

    AvailabilityResult availabilityResult =
        ViatorActivityAvailabilityMapper.mapMultipleDtosToAvailability(
            availabilities, startDate, endDate);

    // Generate availability 3d array using scheduler
    boolean[][][] schedule =
        scheduler.scheduleActivities(availabilityResult.availability(), activities);

    return createDayPlans(
        startDate, totalDays, activities, schedule, availabilityResult.startTimes());
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

      dayPlans.add(DayPlanDTO.builder().date(currentDay).activities(dayActivities).build());
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

  private DayActivityDTO createDayActivity(
      ViatorActivityDTO activity, LocalDate day, String startHour) {

    LocalDateTime startTime = toLocalDateTime(day, startHour);
    LocalDateTime endTime = startTime.plus(getDuration(activity));

    return DayActivityDTO.builder()
        .productCode(activity.productCode())
        .title(activity.title())
        .description(activity.description())
        .startTime(startTime)
        .endTime(endTime)
        .combinedAverageRating(activity.reviews().combinedAverageRating())
        .reviewCount(activity.reviews().totalReviews())
        .fromPrice(activity.pricing().summary().fromPrice())
        .durationMinutes(getDurationMinutes(activity))
        .providerUrl(activity.productUrl())
        .images(activity.images())
        .build();
  }

  private LocalDateTime toLocalDateTime(LocalDate date, String timeStr) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    LocalTime time = LocalTime.parse(timeStr, formatter);
    return date.atTime(time);
  }

  private Duration getDuration(ViatorActivityDTO activity) {
    Integer durationMinutes = getDurationMinutes(activity);
    if (durationMinutes == null) {
      return Duration.ZERO;
    }
    return Duration.ofMinutes(durationMinutes);
  }

  private static Integer getDurationMinutes(ViatorActivityDTO activity) {
    if (activity.duration() == null) {
      return null;
    }

    return activity.duration().fixedDurationInMinutes() != null
        ? activity.duration().fixedDurationInMinutes()
        : activity.duration().variableDurationToMinutes();
  }
}
