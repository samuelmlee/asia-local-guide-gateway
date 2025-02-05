package com.asialocalguide.gateway.core.domain;

import lombok.Getter;

public enum SixHourTimeSlot implements TimeSlot {
  MIDDAY(0, 6, 0, 11, 59),
  AFTERNOON(1, 12, 0, 17, 59),
  EVENING(2, 18, 0, 23, 59);

  @Getter private final int index;
  private final int startHour;
  private final int startMinute;
  private final int endHour;
  private final int endMinute;

  SixHourTimeSlot(int index, int startHour, int startMinute, int endHour, int endMinute) {
    this.index = index;
    this.startHour = startHour;
    this.startMinute = startMinute;
    this.endHour = endHour;
    this.endMinute = endMinute;
  }

  @Override
  public boolean matches(int hour, int minute) {
    return (hour > startHour || (hour == startHour && minute >= startMinute))
        && (hour < endHour || (hour == endHour && minute <= endMinute));
  }
}
