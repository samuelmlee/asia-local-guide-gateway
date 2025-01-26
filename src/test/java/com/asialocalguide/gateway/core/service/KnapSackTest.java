package com.asialocalguide.gateway.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.asialocalguide.gateway.core.dto.DayActivityDTO;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnapsackTest {

  @Test
  void testMaximumValue() {
    // Arrange
    Knapsack knapsack = new Knapsack();

    List<DayActivityDTO> activities =
        Arrays.asList(
            DayActivityDTO.builder()
                .productCode("A1")
                .title("Activity 1")
                .combinedAverageRating(4.5)
                .durationMinutes(2)
                .build(),
            DayActivityDTO.builder()
                .productCode("A2")
                .title("Activity 2")
                .combinedAverageRating(3.0)
                .durationMinutes(1)
                .build(),
            DayActivityDTO.builder()
                .productCode("A3")
                .title("Activity 3")
                .combinedAverageRating(2.0)
                .durationMinutes(3)
                .build());

    int capacity = 4;

    // Act
    double result = knapsack.maximumValue(capacity, activities);

    // Assert
    assertEquals(7.5, result, 0.001);
  }
}
