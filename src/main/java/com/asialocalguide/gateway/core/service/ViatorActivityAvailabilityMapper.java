package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.AvailabilityResult;
import com.asialocalguide.gateway.core.domain.TimeSlot;
import com.asialocalguide.gateway.viator.dto.ViatorActivityAvailabilityDTO;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * A helper class to flatten a single "bookable item" into an object we can treat as a single
 * "activity."
 */
class MappedActivity {
  String productCode;
  // All the seasons (with start/end) and pricing records, etc.
  List<ViatorActivityAvailabilityDTO.Season> seasons;

  MappedActivity(String productCode, List<ViatorActivityAvailabilityDTO.Season> seasons) {
    this.productCode = productCode;
    this.seasons = seasons;
  }
}

public class ViatorActivityAvailabilityMapper {

  private ViatorActivityAvailabilityMapper() {
    // Utility class => private constructor
  }

  /**
   * Combines multiple ViatorActivityAvailabilityDTO objects into a single 3D availability array:
   *
   * <p>[activityIndex][dayIndex][timeSlotIndex]
   *
   * <p>Where: - 'activityIndex' runs over **all** BookableItems from **all** DTOs. - 'dayIndex'
   * covers the range from the earliest startDate to the latest endDate found. - 'timeSlotIndex'
   * covers every unique startTime found in all TimedEntries.
   *
   * <p>The returned array element is TRUE if that (activity, day, timeSlot) is available, FALSE
   * otherwise.
   */
  public static AvailabilityResult mapMultipleDtosToAvailability(
      List<ViatorActivityAvailabilityDTO> dtos, LocalDate minDate, LocalDate maxDate) {

    if (dtos.isEmpty() || minDate == null || maxDate == null) {
      // Input data invalid => empty array
      return new AvailabilityResult(new boolean[0][0][0], new String[0][0][0]);
    }

    // 1) Flatten all BookableItems
    List<MappedActivity> allActivities = flattenDtos(dtos);

    // 2) Build day list
    List<LocalDate> allDates = buildDateList(minDate, maxDate);

    // 3) Initialize array: # of activities × # of days × # of timeslots
    // Timeslots: morning, afternoon, evening
    int numTimeSlots = 3;

    // Initialize the 3D array: [activityIndex][dayIndex][timeSlotIndex]
    boolean[][][] availability = new boolean[allActivities.size()][allDates.size()][numTimeSlots];
    String[][][] startTimes = new String[allActivities.size()][allDates.size()][numTimeSlots];

    // Return the filled 3D array
    fillAvailability(allActivities, allDates, availability, startTimes);

    return new AvailabilityResult(availability, startTimes);
  }

  private static void fillAvailability(
      List<MappedActivity> allActivities,
      List<LocalDate> allDates,
      boolean[][][] availability,
      String[][][] startTimes) {

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    for (int a = 0; a < allActivities.size(); a++) {
      MappedActivity activity = allActivities.get(a);

      for (ViatorActivityAvailabilityDTO.Season season : activity.seasons) {
        LocalDate seasonStart = LocalDate.parse(season.startDate(), dateFormatter);
        LocalDate seasonEnd =
            (season.endDate() == null || season.endDate().isBlank())
                ? LocalDate.MAX
                : LocalDate.parse(season.endDate(), dateFormatter);

        for (ViatorActivityAvailabilityDTO.PricingRecord pricingRecord : season.pricingRecords()) {
          List<DayOfWeek> allowedDaysOfWeek = parseDaysOfWeek(pricingRecord.daysOfWeek());

          for (ViatorActivityAvailabilityDTO.TimedEntry entry : pricingRecord.timedEntries()) {
            // Resolve index from startTime, 0 for time before 12:00, 1 for 12:00-18:00, 2 for later
            // than 18:00
            int tIndex = mapTimeToTimeslot(entry.startTime());
            Set<String> unavailable = new HashSet<>();
            for (ViatorActivityAvailabilityDTO.UnavailableDate ud : entry.unavailableDates()) {
              unavailable.add(ud.date());
            }

            for (int d = 0; d < allDates.size(); d++) {
              LocalDate date = allDates.get(d);

              if (date.isBefore(seasonStart)
                  || date.isAfter(seasonEnd)
                  || !allowedDaysOfWeek.contains(date.getDayOfWeek())
                  || unavailable.contains(date.format(dateFormatter))) {
                continue;
              }

              availability[a][d][tIndex] = true;

              // Ensure we store the earliest time per time slot
              if (startTimes[a][d][tIndex] == null
                  || entry.startTime().compareTo(startTimes[a][d][tIndex]) < 0) {
                startTimes[a][d][tIndex] = entry.startTime();
              }
            }
          }
        }
      }
    }
  }

  private static List<MappedActivity> flattenDtos(List<ViatorActivityAvailabilityDTO> dtos) {
    List<MappedActivity> activities = new ArrayList<>();
    for (ViatorActivityAvailabilityDTO dto : dtos) {
      String productCode = dto.productCode();
      // Only considering first default bookable item to not pollute the calendar with multiple
      // entries of the same activity
      ViatorActivityAvailabilityDTO.BookableItem item = dto.bookableItems().getFirst();
      activities.add(new MappedActivity(productCode, item.seasons()));
    }
    return activities;
  }

  private static List<LocalDate> buildDateList(LocalDate minDate, LocalDate maxDate) {
    List<LocalDate> dates = new ArrayList<>();
    LocalDate cursor = minDate;
    while (!cursor.isAfter(maxDate)) {
      dates.add(cursor);
      cursor = cursor.plusDays(1);
    }
    return dates;
  }

  /** Helper to parse the "daysOfWeek" strings (e.g. "MONDAY", "TUESDAY") to DayOfWeek enums. */
  private static List<DayOfWeek> parseDaysOfWeek(List<String> daysOfWeekStrs) {
    if (daysOfWeekStrs == null) return Collections.emptyList();
    List<DayOfWeek> result = new ArrayList<>();
    for (String s : daysOfWeekStrs) {
      // e.g., "MONDAY" -> DayOfWeek.MONDAY
      if (s != null) {
        try {
          result.add(DayOfWeek.valueOf(s));
        } catch (IllegalArgumentException e) {
          // If an unknown day string is provided, skip or handle error
        }
      }
    }
    return result;
  }

  private static int mapTimeToTimeslot(String time) {
    return TimeSlot.getIndexFromTime(time);
  }
}
