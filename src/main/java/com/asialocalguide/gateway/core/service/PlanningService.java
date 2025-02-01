package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.dto.ActivityPlanningRequestDTO;
import com.asialocalguide.gateway.core.dto.DayActivityDTO;
import com.asialocalguide.gateway.core.dto.DayPlanDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDetailDTO;
import java.time.*;
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

    // TODO: redo all implementation logic for generating day plans

    List<ViatorActivityDTO> activities =
        activityDetails.stream().map(ViatorActivityDetailDTO::activity).toList();

    LocalDate startDate = request.startDate();
    LocalDate endDate = request.endDate();

    long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
    return createDayPlans(startDate, totalDays, activities);
  }

  private List<DayPlanDTO> createDayPlans(
      LocalDate startDate, long totalDays, List<ViatorActivityDTO> activities) {
    List<DayPlanDTO> dayPlans = new ArrayList<>();
    int activityIndex = 0;

    for (int dayOffset = 0; dayOffset < totalDays; dayOffset++) {
      LocalDate currentDay = startDate.plusDays(dayOffset);

      List<DayActivityDTO> dayActivities =
          assignActivitiesForDay(activities, activityIndex, currentDay);
      activityIndex += dayActivities.size();

      dayPlans.add(DayPlanDTO.builder().date(currentDay).activities(dayActivities).build());
    }

    return dayPlans;
  }

  private List<DayActivityDTO> assignActivitiesForDay(
      List<ViatorActivityDTO> activities, int startIndex, LocalDate day) {
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
      ViatorActivityDTO activity, LocalDate day, int startHour, int endHour) {
    LocalDateTime startTime = toLocalDateTime(day, startHour);
    LocalDateTime endTime = startTime.withHour(endHour);

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

  private LocalDateTime toLocalDateTime(LocalDate date, int hour) {
    if (hour < 0 || hour > 23) {
      throw new IllegalArgumentException("Hour must be between 0 and 23");
    }

    return date.atTime(hour, 0);
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
