package com.asialocalguide.gateway.core.domain;

import lombok.Getter;

public enum TimeSlot {
  MORNING(0, 0, 11), // 00:00 - 11:59
  AFTERNOON(1, 12, 17), // 12:00 - 17:59
  EVENING(2, 18, 23); // 18:00 - 23:59

  @Getter private final int index;
  private final int startHour;
  private final int endHour;

  TimeSlot(int index, int startHour, int endHour) {
    this.index = index;
    this.startHour = startHour;
    this.endHour = endHour;
  }

  /**
   * Determines the time slot for a given hour.
   *
   * @param hour The hour of the day (0-23).
   * @return The corresponding TimeSlot.
   */
  public static TimeSlot fromHour(int hour) {
    for (TimeSlot slot : values()) {
      if (hour >= slot.startHour && hour <= slot.endHour) {
        return slot;
      }
    }
    throw new IllegalArgumentException("Invalid hour: " + hour);
  }

  /**
   * Determines the time slot index for a given string time (HH:mm).
   *
   * @param time The time in "HH:mm" format.
   * @return The corresponding time slot index (0, 1, or 2).
   */
  public static int getIndexFromTime(String time) {
    int hour = Integer.parseInt(time.split(":")[0]);
    return fromHour(hour).getIndex();
  }
}
