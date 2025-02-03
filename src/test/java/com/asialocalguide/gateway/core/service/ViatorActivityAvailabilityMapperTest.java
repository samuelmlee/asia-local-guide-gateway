package com.asialocalguide.gateway.core.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.asialocalguide.gateway.viator.dto.ViatorActivityAvailabilityDTO;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ViatorActivityAvailabilityMapperTest {

  private final ViatorActivityAvailabilityMapper mapper = new ViatorActivityAvailabilityMapper();

  /** Test: Empty DTO list should return an empty 3D boolean array */
  @Test
  void testMapMultipleDtosToAvailability_EmptyInput() {
    boolean[][][] availability =
        mapper.mapMultipleDtosToAvailability(
            Collections.emptyList(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 10));

    // Expected an empty array for empty DTO list.
    assertThat(availability).isEmpty();
  }

  /** Test: Null dates should return an empty array */
  @Test
  void testMapMultipleDtosToAvailability_NullDates() {
    boolean[][][] availability =
        mapper.mapMultipleDtosToAvailability(
            List.of(mockViatorActivityAvailabilityDTO()), null, null);

    // Expected an empty array for null date inputs.
    assertThat(availability).isEmpty();
  }

  /** Test: Valid DTO with a single available activity */
  @Test
  void testMapMultipleDtosToAvailability_ValidSingleActivity() {
    LocalDate minDate = LocalDate.of(2025, 1, 1);
    LocalDate maxDate = LocalDate.of(2025, 1, 5);

    boolean[][][] availability =
        mapper.mapMultipleDtosToAvailability(
            List.of(mockViatorActivityAvailabilityDTO()), minDate, maxDate);

    // Expected one activity entry.
    assertThat(availability.length).isEqualTo(1);
    // Expected 5 days of data.
    assertThat(availability[0].length).isEqualTo(5);
    // Expected 3 time slots.
    assertThat(availability[0][0].length).isEqualTo(3);
    // Expected activity to be available in afternoon slot.
    assertThat(availability[0][0][1]).isTrue();
  }

  /** Test: Activity is unavailable on a specific date */
  @Test
  void testMapMultipleDtosToAvailability_UnavailableDate() {
    LocalDate minDate = LocalDate.of(2025, 1, 1);
    LocalDate maxDate = LocalDate.of(2025, 1, 5);

    boolean[][][] availability =
        mapper.mapMultipleDtosToAvailability(
            List.of(mockViatorActivityAvailabilityDTOWithUnavailableDate("2025-01-03")),
            minDate,
            maxDate);

    assertFalse(
        availability[0][2][1], "Expected activity to be unavailable on Jan 3 in afternoon.");
  }

  /** Test: Activity is available in multiple time slots */
  @Test
  void testMapMultipleDtosToAvailability_MultipleTimeSlots() {
    LocalDate minDate = LocalDate.of(2025, 1, 1);
    LocalDate maxDate = LocalDate.of(2025, 1, 5);

    boolean[][][] availability =
        mapper.mapMultipleDtosToAvailability(
            List.of(mockViatorActivityAvailabilityDTOWithMultipleTimes()), minDate, maxDate);

    assertTrue(availability[0][0][0], "Expected activity to be available in morning.");
    assertTrue(availability[0][0][2], "Expected activity to be available in evening.");
  }

  /** Test: No endDate should use a far-future date */
  @Test
  void testMapMultipleDtosToAvailability_NoEndDate() {
    LocalDate minDate = LocalDate.of(2025, 1, 1);
    LocalDate maxDate = LocalDate.of(2025, 1, 5);

    boolean[][][] availability =
        mapper.mapMultipleDtosToAvailability(
            List.of(mockViatorActivityAvailabilityDTOWithoutEndDate()), minDate, maxDate);

    assertTrue(availability[0][0][1], "Expected activity to still be available far in the future.");
  }

  // --------------------------- MOCK DATA HELPERS ---------------------------

  private ViatorActivityAvailabilityDTO mockViatorActivityAvailabilityDTO() {
    return new ViatorActivityAvailabilityDTO(
        "ACT123",
        List.of(
            new ViatorActivityAvailabilityDTO.BookableItem(
                "DEFAULT",
                List.of(
                    new ViatorActivityAvailabilityDTO.Season(
                        "2025-01-01",
                        "2025-01-05",
                        List.of(
                            new ViatorActivityAvailabilityDTO.PricingRecord(
                                List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"),
                                List.of(
                                    new ViatorActivityAvailabilityDTO.TimedEntry(
                                        "14:00", List.of())))))))),
        "USD",
        new ViatorActivityAvailabilityDTO.Summary(65.00));
  }

  private ViatorActivityAvailabilityDTO mockViatorActivityAvailabilityDTOWithUnavailableDate(
      String unavailableDate) {
    return new ViatorActivityAvailabilityDTO(
        "ACT123",
        List.of(
            new ViatorActivityAvailabilityDTO.BookableItem(
                "DEFAULT",
                List.of(
                    new ViatorActivityAvailabilityDTO.Season(
                        "2025-01-01",
                        "2025-01-05",
                        List.of(
                            new ViatorActivityAvailabilityDTO.PricingRecord(
                                List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"),
                                List.of(
                                    new ViatorActivityAvailabilityDTO.TimedEntry(
                                        "14:00",
                                        List.of(
                                            new ViatorActivityAvailabilityDTO.UnavailableDate(
                                                unavailableDate, "SOLD_OUT")))))))))),
        "USD",
        new ViatorActivityAvailabilityDTO.Summary(65.00));
  }

  private ViatorActivityAvailabilityDTO mockViatorActivityAvailabilityDTOWithMultipleTimes() {
    return new ViatorActivityAvailabilityDTO(
        "ACT123",
        List.of(
            new ViatorActivityAvailabilityDTO.BookableItem(
                "DEFAULT",
                List.of(
                    new ViatorActivityAvailabilityDTO.Season(
                        "2025-01-01",
                        "2025-01-05",
                        List.of(
                            new ViatorActivityAvailabilityDTO.PricingRecord(
                                List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"),
                                List.of(
                                    new ViatorActivityAvailabilityDTO.TimedEntry(
                                        "08:30", List.of()),
                                    new ViatorActivityAvailabilityDTO.TimedEntry(
                                        "19:00", List.of())))))))),
        "USD",
        new ViatorActivityAvailabilityDTO.Summary(65.00));
  }

  private ViatorActivityAvailabilityDTO mockViatorActivityAvailabilityDTOWithoutEndDate() {
    return new ViatorActivityAvailabilityDTO(
        "ACT123",
        List.of(
            new ViatorActivityAvailabilityDTO.BookableItem(
                "DEFAULT",
                List.of(
                    new ViatorActivityAvailabilityDTO.Season(
                        "2025-01-01",
                        null,
                        List.of(
                            new ViatorActivityAvailabilityDTO.PricingRecord(
                                List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"),
                                List.of(
                                    new ViatorActivityAvailabilityDTO.TimedEntry(
                                        "14:00", List.of())))))))),
        "USD",
        new ViatorActivityAvailabilityDTO.Summary(65.00));
  }
}
