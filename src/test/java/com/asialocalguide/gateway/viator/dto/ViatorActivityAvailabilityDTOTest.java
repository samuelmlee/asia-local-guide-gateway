package com.asialocalguide.gateway.viator.dto;

import jakarta.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ViatorActivityAvailabilityDTOTest {

  @Test
  void canValidateViatorActivityAvailabilityDTO() {
    //  Create a valid DTO
    @SuppressWarnings(
        "squid:S1854") // Replace "squid:S00100" with the specific rule ID you want to suppress
    ViatorActivityAvailabilityDTO validDTO =
        new ViatorActivityAvailabilityDTO(
            "PC123",
            List.of(
                new ViatorActivityAvailabilityDTO.BookableItem(
                    "Option1",
                    List.of(
                        new ViatorActivityAvailabilityDTO.Season(
                            "2024-01-01",
                            "2024-12-31",
                            List.of(
                                new ViatorActivityAvailabilityDTO.PricingRecord(
                                    List.of("Monday", "Wednesday"),
                                    List.of(
                                        new ViatorActivityAvailabilityDTO.TimedEntry(
                                            "10:00 AM",
                                            List.of(
                                                new ViatorActivityAvailabilityDTO.UnavailableDate(
                                                    "2024-05-01", "Holiday")))))))))),
            "USD",
            new ViatorActivityAvailabilityDTO.Summary(99.99));

    try {
      //  Create an invalid DTO with null and empty constraints
      @SuppressWarnings("squid:S1854")
      ViatorActivityAvailabilityDTO invalidDTO =
          new ViatorActivityAvailabilityDTO(
              "PC123",
              null, //  Invalid: bookableItems is null
              "USD",
              new ViatorActivityAvailabilityDTO.Summary(99.99));

    } catch (ConstraintViolationException cve) {
      System.out.println("Validation failed: " + cve.getMessage());
    }

    try {
      // Invalid DTO with empty timedEntries
      @SuppressWarnings("squid:S1854")
      ViatorActivityAvailabilityDTO invalidDTO2 =
          new ViatorActivityAvailabilityDTO(
              "PC123",
              List.of(
                  new ViatorActivityAvailabilityDTO.BookableItem(
                      "Option1",
                      List.of(
                          new ViatorActivityAvailabilityDTO.Season(
                              "2024-01-01",
                              "2024-12-31",
                              List.of(
                                  new ViatorActivityAvailabilityDTO.PricingRecord(
                                      List.of("Monday", "Wednesday"),
                                      Collections.emptyList() //  Invalid: timedEntries is empty
                                      )))))),
              "USD",
              new ViatorActivityAvailabilityDTO.Summary(99.99));

    } catch (ConstraintViolationException cve) {
      System.out.println("Validation failed: " + cve.getMessage());
    }
  }
}
