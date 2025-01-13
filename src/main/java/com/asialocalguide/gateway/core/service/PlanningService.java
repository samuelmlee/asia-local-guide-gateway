package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.dto.ActivityPlanningDTO;
import com.asialocalguide.gateway.core.dto.ActivityPlanningRequestDTO;
import com.asialocalguide.gateway.core.dto.DayActivityDTO;
import com.asialocalguide.gateway.core.dto.DayScheduleDTO;
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

  public ActivityPlanningDTO generateActivityPlanning(ActivityPlanningRequestDTO request) {
    SupportedLocale locale = SupportedLocale.getDefaultLocale();
    List<ViatorActivityDTO> activities = activityService.getActivities(locale, request);

    Instant startDate = request.startDateISO();
    Instant endDate = request.endDateISO();

    List<DayScheduleDTO> daySchedules = new ArrayList<>();

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

      daySchedules.add(
          DayScheduleDTO.builder().date(zonedDateTime).activities(dayActivities).build());
    }

    return new ActivityPlanningDTO(daySchedules);
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
        .build();
  }
}
