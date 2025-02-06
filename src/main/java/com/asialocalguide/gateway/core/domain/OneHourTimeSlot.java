package com.asialocalguide.gateway.core.domain;

import lombok.Getter;

@Getter
public enum OneHourTimeSlot {
  SLOT_6AM(0, 6, 0, 6, 59),
  SLOT_7AM(1, 7, 0, 7, 59),
  SLOT_8AM(2, 8, 0, 8, 59),
  SLOT_9AM(3, 9, 0, 9, 59),
  SLOT_10AM(4, 10, 0, 10, 59),
  SLOT_11AM(5, 11, 0, 11, 59),
  SLOT_12PM(6, 12, 0, 12, 59),
  SLOT_1PM(7, 13, 0, 13, 59),
  SLOT_2PM(8, 14, 0, 14, 59),
  SLOT_3PM(9, 15, 0, 15, 59),
  SLOT_4PM(10, 16, 0, 16, 59),
  SLOT_5PM(11, 17, 0, 17, 59),
  SLOT_6PM(12, 18, 0, 18, 59),
  SLOT_7PM(13, 19, 0, 19, 59),
  SLOT_8PM(14, 20, 0, 20, 59),
  SLOT_9PM(15, 21, 0, 21, 59),
  SLOT_10PM(16, 22, 0, 22, 59),
  SLOT_11PM(17, 23, 0, 23, 59),
  SLOT_0AM(18, 0, 0, 0, 59),
  SLOT_1AM(19, 1, 0, 1, 59),
  SLOT_2AM(20, 2, 0, 2, 59),
  SLOT_3AM(21, 3, 0, 3, 59),
  SLOT_4AM(22, 4, 0, 4, 59),
  SLOT_5AM(23, 5, 0, 5, 59);

  private final int index;
  private final int startHour;
  private final int startMinute;
  private final int endHour;
  private final int endMinute;

  OneHourTimeSlot(int index, int startHour, int startMinute, int endHour, int endMinute) {
    this.index = index;
    this.startHour = startHour;
    this.startMinute = startMinute;
    this.endHour = endHour;
    this.endMinute = endMinute;
  }

  public static int getIndexFromTimeString(String time) {
    String[] parts = time.split(":");
    int hour = Integer.parseInt(parts[0]);
    int minute = Integer.parseInt(parts[1]);

    for (OneHourTimeSlot slot : OneHourTimeSlot.values()) {
      if ((hour > slot.startHour || (hour == slot.startHour && minute >= slot.startMinute))
          && (hour < slot.endHour || (hour == slot.endHour && minute <= slot.endMinute))) {
        return slot.getIndex();
      }
    }
    throw new IllegalArgumentException("Invalid time: " + time);
  }

  public static int getDurationInSlots(int durationInMinutes) {
    // Apply mapping rules
    // Occupies one slot per hour, rounded up
    return Math.max(1, (int) Math.ceil((double) durationInMinutes / 60));
  }
}
