package com.asialocalguide.gateway.core.service;

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
  public boolean[][][] mapMultipleDtosToAvailability(
      List<ViatorActivityAvailabilityDTO> dtos, LocalDate minDate, LocalDate maxDate) {

    if (minDate == null || maxDate == null) {
      // No valid date range => empty array
      return new boolean[dtos.size()][0][0];
    }

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 1) Flatten all BookableItems from all DTOs into a list of MappedActivity
    //    so each "activity" is one (productCode, productOptionCode) pair.
    List<MappedActivity> allActivities = new ArrayList<>();

    for (ViatorActivityAvailabilityDTO dto : dtos) {
      String productCode = dto.productCode();

      // Only considering first default bookable item to not pollute the calendar with multiple
      // activities of the same type
      ViatorActivityAvailabilityDTO.BookableItem item = dto.bookableItems().getFirst();
      allActivities.add(new MappedActivity(productCode, item.seasons()));
    }

    int numActivities = allActivities.size();
    if (numActivities == 0) {
      // No data => empty array
      return new boolean[0][0][0];
    }

    // 2) Create a list of all the days from minDate to maxDate
    List<LocalDate> allDates = new ArrayList<>();
    LocalDate cursor = minDate;
    while (!cursor.isAfter(maxDate)) {
      allDates.add(cursor);
      cursor = cursor.plusDays(1);
    }
    int numDays = allDates.size();

    // 3 Timeslots: morning, afternoon, evening
    int numTimeSlots = 3;

    // Initialize the 3D array: [activityIndex][dayIndex][timeSlotIndex]
    boolean[][][] availability = new boolean[numActivities][numDays][numTimeSlots];

    // 4) Fill availability
    for (int a = 0; a < numActivities; a++) {
      MappedActivity activity = allActivities.get(a);

      // For each season in this activity
      for (ViatorActivityAvailabilityDTO.Season season : activity.seasons) {
        LocalDate seasonStart = LocalDate.parse(season.startDate(), dateFormatter);

        // If the API sometimes does not provide an endDate, handle that logic
        // here assume endDate is always present for simplicity:
        LocalDate seasonEnd =
            (season.endDate() == null || season.endDate().isBlank())
                ? seasonStart
                : LocalDate.parse(season.endDate(), dateFormatter);

        // For each PricingRecord
        for (ViatorActivityAvailabilityDTO.PricingRecord record : season.pricingRecords()) {
          // Convert daysOfWeek to DayOfWeek enums
          List<DayOfWeek> allowedDaysOfWeek = parseDaysOfWeek(record.daysOfWeek());

          // For each TimedEntry => get the timeSlot index
          for (ViatorActivityAvailabilityDTO.TimedEntry entry : record.timedEntries()) {
            int tIndex = mapTimeToTimeslot(entry.startTime());

            // Gather all unavailableDates as strings for quick checking
            Set<String> unavailable = new HashSet<>();
            for (ViatorActivityAvailabilityDTO.UnavailableDate ud : entry.unavailableDates()) {
              unavailable.add(ud.date()); // e.g. "2025-05-20"
            }

            // Fill in days within [minDate, maxDate]
            // that match allowed dayOfWeek and are not in 'unavailable'
            for (int d = 0; d < allDates.size(); d++) {
              LocalDate date = allDates.get(d);

              if (date.isBefore(seasonStart) || date.isAfter(seasonEnd)) {
                // Outside the season => remain false
                continue;
              }
              if (!allowedDaysOfWeek.contains(date.getDayOfWeek())) {
                // Not an allowed day => remain false
                continue;
              }
              if (unavailable.contains(date.format(dateFormatter))) {
                // Specifically unavailable => remain false
                continue;
              }

              // Otherwise => available
              availability[a][d][tIndex] = true;
            }
          }
        }
      }
    }

    // Return the combined 3D array
    return availability;
  }

  /** Helper to parse the "daysOfWeek" strings (e.g. "MONDAY", "TUESDAY") to DayOfWeek enums. */
  private List<DayOfWeek> parseDaysOfWeek(List<String> daysOfWeekStrs) {
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

  public static int mapTimeToTimeslot(String time) {
    // Parse time string into hour and minute
    String[] parts = time.split(":");
    int hour = Integer.parseInt(parts[0]);

    // Define time slot categories
    if (hour >= 0 && hour < 12) {
      return 0; // Morning
    } else if (hour >= 12 && hour < 18) {
      return 1; // Afternoon
    } else {
      return 2; // Evening
    }
  }
}
