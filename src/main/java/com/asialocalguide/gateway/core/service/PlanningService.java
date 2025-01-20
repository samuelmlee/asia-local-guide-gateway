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

    long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
    return createDayPlans(startDate, totalDays, activities);
  }

  private List<DayPlanDTO> createDayPlans(
      Instant startDate, long totalDays, List<ViatorActivityDTO> activities) {
    List<DayPlanDTO> dayPlans = new ArrayList<>();
    int activityIndex = 0;

    for (int dayOffset = 0; dayOffset < totalDays; dayOffset++) {
      Instant currentDay = startDate.plus(dayOffset, ChronoUnit.DAYS);
      ZonedDateTime zonedDateTime = currentDay.atZone(ZoneId.of("UTC"));

      List<DayActivityDTO> dayActivities =
          assignActivitiesForDay(activities, activityIndex, currentDay);
      activityIndex += dayActivities.size();

      dayPlans.add(DayPlanDTO.builder().date(zonedDateTime).activities(dayActivities).build());
    }

    return dayPlans;
  }

  private List<DayActivityDTO> assignActivitiesForDay(
      List<ViatorActivityDTO> activities, int startIndex, Instant day) {
    List<DayActivityDTO> dayActivities = new ArrayList<>();

    if (startIndex < activities.size()) {
      dayActivities.add(createDayActivity(activities.get(startIndex), day, 9, 12));
    }
    if (startIndex + 1 < activities.size()) {
      dayActivities.add(createDayActivity(activities.get(startIndex + 1), day, 14, 17));
    }

    return dayActivities;
  }

  private DayActivityDTO createDayActivity(
      ViatorActivityDTO activity, Instant day, int startHour, int endHour) {
    ZonedDateTime startTime = toZonedDateTime(day, startHour);
    ZonedDateTime endTime = startTime.withHour(endHour);

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
        .images(activity.images())
        .build();
  }

  private ZonedDateTime toZonedDateTime(Instant day, int hour) {
    return day.atZone(ZoneId.of("UTC")).withHour(hour).withMinute(0).withSecond(0).withNano(0);
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
