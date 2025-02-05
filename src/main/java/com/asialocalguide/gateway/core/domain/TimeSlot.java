package com.asialocalguide.gateway.core.domain;

public interface TimeSlot {
  int getIndex();

  boolean matches(int hour, int minute);

  /**
   * Retrieves the index from a time string dynamically for any TimeSlot enum. Supports both
   * hour-based and 30-minute slot-based implementations.
   *
   * @param time The time in "HH:mm" format.
   * @param timeSlotEnum The enum class implementing TimeSlot.
   * @return The corresponding time slot index.
   */
  static int getIndexFromTimeString(String time, Class<? extends TimeSlot> timeSlotEnum) {
    String[] parts = time.split(":");
    int hour = Integer.parseInt(parts[0]);
    int minute = Integer.parseInt(parts[1]);

    for (TimeSlot slot : timeSlotEnum.getEnumConstants()) {
      if (slot.matches(hour, minute)) {
        return slot.getIndex();
      }
    }
    throw new IllegalArgumentException("Invalid time: " + time);
  }
}
