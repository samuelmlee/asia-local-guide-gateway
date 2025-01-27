package com.asialocalguide.gateway.core.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.asialocalguide.gateway.core.dto.DayActivityDTO;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class KnapsackSelecterTest {

  @Test
  void testMaximumValue() {
    // Arrange
    KnapsackSelector knapsackSelector = new KnapsackSelector();

    List<DayActivityDTO> activities =
        Arrays.asList(
            DayActivityDTO.builder()
                .productCode("A1")
                .title("Activity 1")
                .combinedAverageRating(4.5)
                .durationMinutes(2)
                .build(),
            DayActivityDTO.builder()
                .productCode("A3")
                .title("Activity 3")
                .combinedAverageRating(2.0)
                .durationMinutes(3)
                .build(),
            DayActivityDTO.builder()
                .productCode("A2")
                .title("Activity 2")
                .combinedAverageRating(3.0)
                .durationMinutes(1)
                .build());

    int capacity = 4;

    // Act
    List<DayActivityDTO> selection = knapsackSelector.selectBestActivities(activities, capacity);

    // Assert
    assertThat(selection).hasSize(2);
  }
}
