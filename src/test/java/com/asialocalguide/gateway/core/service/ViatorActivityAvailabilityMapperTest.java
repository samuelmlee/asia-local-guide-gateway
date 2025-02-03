package com.asialocalguide.gateway.core.service;

import static org.assertj.core.api.Assertions.*;

import com.asialocalguide.gateway.core.domain.AvailabilityResult;
import com.asialocalguide.gateway.viator.dto.ViatorActivityAvailabilityDTO;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ViatorActivityAvailabilityMapperTest {

  /** Test: Empty DTO list should return an empty 3D boolean array and empty start times */
  @Test
  void testMapMultipleDtosToAvailability_EmptyInput() {
    AvailabilityResult result =
        ViatorActivityAvailabilityMapper.mapMultipleDtosToAvailability(
            Collections.emptyList(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 10));

    assertThat(result.availability()).isEmpty();
    assertThat(result.startTimes()).isEmpty();
  }

  /** Test: Null dates should return an empty result */
  @Test
  void testMapMultipleDtosToAvailability_NullDates() {
    AvailabilityResult result =
        ViatorActivityAvailabilityMapper.mapMultipleDtosToAvailability(
            List.of(mockViatorActivityAvailabilityDTO()), null, null);

    assertThat(result.availability()).isEmpty();
    assertThat(result.startTimes()).isEmpty();
  }

  /** Test: Valid DTO with a single available activity */
  @Test
  void testMapMultipleDtosToAvailability_ValidSingleActivity() {
    LocalDate minDate = LocalDate.of(2025, 1, 1);
    LocalDate maxDate = LocalDate.of(2025, 1, 5);

    AvailabilityResult result =
        ViatorActivityAvailabilityMapper.mapMultipleDtosToAvailability(
            List.of(mockViatorActivityAvailabilityDTO()), minDate, maxDate);

    boolean[][][] availability = result.availability();
    String[][][] startTimes = result.startTimes();

    // Expected one activity entry.
    assertThat(availability.length).isEqualTo(1);
    assertThat(startTimes.length).isEqualTo(1);

    // Expected 5 days of data.
    assertThat(availability[0].length).isEqualTo(5);
    assertThat(startTimes[0].length).isEqualTo(5);

    // Expected 3 time slots.
    assertThat(availability[0][0].length).isEqualTo(3);
    assertThat(startTimes[0][0].length).isEqualTo(3);

    // Expected activity to be available in the afternoon slot.
    assertThat(availability[0][0][1]).isTrue();
    assertThat(startTimes[0][0][1]).isEqualTo("14:00");
  }

  /** Test: Activity is unavailable on a specific date */
  @Test
  void testMapMultipleDtosToAvailability_UnavailableDate() {
    LocalDate minDate = LocalDate.of(2025, 1, 1);
    LocalDate maxDate = LocalDate.of(2025, 1, 5);

    AvailabilityResult result =
        ViatorActivityAvailabilityMapper.mapMultipleDtosToAvailability(
            List.of(mockViatorActivityAvailabilityDTOWithUnavailableDate("2025-01-03")),
            minDate,
            maxDate);

    boolean[][][] availability = result.availability();

    assertThat(availability[0][2][1]).isFalse();
  }

  /** Test: Activity is available in multiple time slots */
  @Test
  void testMapMultipleDtosToAvailability_MultipleTimeSlots() {
    LocalDate minDate = LocalDate.of(2025, 1, 1);
    LocalDate maxDate = LocalDate.of(2025, 1, 5);

    AvailabilityResult result =
        ViatorActivityAvailabilityMapper.mapMultipleDtosToAvailability(
            List.of(mockViatorActivityAvailabilityDTOWithMultipleTimes()), minDate, maxDate);

    boolean[][][] availability = result.availability();
    String[][][] startTimes = result.startTimes();

    assertThat(availability[0][0][0]).isTrue();
    assertThat(startTimes[0][0][0]).isEqualTo("08:30");

    assertThat(availability[0][0][2]).isTrue();
    assertThat(startTimes[0][0][2]).isEqualTo("19:00");
  }

  /** Test: No endDate should use a far-future date */
  @Test
  void testMapMultipleDtosToAvailability_NoEndDate() {
    LocalDate minDate = LocalDate.of(2025, 1, 1);
    LocalDate maxDate = LocalDate.of(2025, 1, 5);

    AvailabilityResult result =
        ViatorActivityAvailabilityMapper.mapMultipleDtosToAvailability(
            List.of(mockViatorActivityAvailabilityDTOWithoutEndDate()), minDate, maxDate);

    boolean[][][] availability = result.availability();

    assertThat(availability[0][0][1]).isTrue();
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
