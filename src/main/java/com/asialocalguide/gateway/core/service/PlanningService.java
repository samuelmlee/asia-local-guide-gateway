package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.dto.ActivityPlanningRequestDTO;
import com.asialocalguide.gateway.core.dto.DayActivityDTO;
import com.asialocalguide.gateway.core.dto.DayPlanDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    List<ViatorActivityDTO> activities = activityService.getActivities(locale, request);

    Instant startDate = request.startDate();
    Instant endDate = request.endDate();

    List<DayPlanDTO> dayPlanDTOS = new ArrayList<>();

    // Calculate the number of days between startDate and endDate (inclusive)
    long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

    // Index to track the current activity from the list
    int activityIndex = 0;

    // Loop through each day in the date range
    for (long i = 0; i < totalDays; i++) {
      Instant currentDay = startDate.plus(i, ChronoUnit.DAYS);

      // Convert Instant to ZonedDateTime in UTC
      ZonedDateTime zonedDateTime = currentDay.atZone(ZoneId.of("UTC"));

      List<DayActivityDTO> dayActivities = new ArrayList<>();

      // Assign morning and afternoon activities for the day if available
      if (activityIndex < activities.size()) {
        dayActivities.add(createDayActivity(activities.get(activityIndex), currentDay, 9, 12));
        activityIndex++;
      }
      if (activityIndex < activities.size()) {
        dayActivities.add(createDayActivity(activities.get(activityIndex), currentDay, 14, 17));
        activityIndex++;
      }

      dayPlanDTOS.add(DayPlanDTO.builder().date(zonedDateTime).activities(dayActivities).build());
    }

    return dayPlanDTOS;
  }

  private DayActivityDTO createDayActivity(
      ViatorActivityDTO activity, Instant day, int startHour, int endHour) {
    // Convert Instant to ZonedDateTime in UTC
    ZonedDateTime startZoned =
        day.atZone(ZoneId.of("UTC")).withHour(startHour).withMinute(0).withSecond(0).withNano(0);
    ZonedDateTime endZoned = startZoned.withHour(endHour);

    // Build and return DayActivityDTO
    return DayActivityDTO.builder()
        .productCode(activity.productCode())
        .title(activity.title())
        .description(activity.description())
        .startTime(startZoned)
        .endTime(endZoned)
        .combinedAverageRating(activity.reviews().combinedAverageRating())
        .reviewCount(activity.reviews().totalReviews())
        .fromPrice(activity.pricing().summary().fromPrice())
        .durationMinutes(getDurationMinutes(activity))
        .images(activity.images())
        .build();
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
