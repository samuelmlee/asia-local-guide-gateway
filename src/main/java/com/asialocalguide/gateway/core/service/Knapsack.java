package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.dto.DayActivityDTO;
import java.util.List;

public class Knapsack {

  private double[][] maxValues;
  private List<DayActivityDTO> activities;

  double maximumValue(int capacity, List<DayActivityDTO> items) {
    maxValues = new double[items.size() + 1][capacity + 1];
    this.activities = items;

    return maxValueInRange(items.size(), capacity);
  }

  private double maxValueInRange(int itemSize, int capacity) {
    if (isOutOfBounds(itemSize, capacity)) {
      return 0;
    }
    int previousRow = itemSize - 1;
    if (shouldCalculate(previousRow, capacity)) {
      maxValues[previousRow][capacity] = maxValueInRange(previousRow, capacity);
    }
    if (itemWeightAt(itemSize) > capacity) {
      maxValues[itemSize][capacity] = maxValues[previousRow][capacity];
    } else {
      int previousColumn = capacity - itemWeightAt(itemSize);
      if (shouldCalculate(previousRow, previousColumn)) {
        maxValues[previousRow][previousColumn] = maxValueInRange(previousRow, previousColumn);
      }
      maxValues[itemSize][capacity] =
          Math.max(
              maxValues[previousRow][capacity],
              maxValues[previousRow][previousColumn] + itemValueAt(itemSize));
    }
    return maxValues[itemSize][capacity];
  }

  private boolean shouldCalculate(int row, int column) {
    return maxValues[row][column] == -1;
  }

  private static boolean isOutOfBounds(int itemSize, int capacity) {
    return itemSize == 0 || capacity <= 0;
  }

  private int itemWeightAt(int index) {
    return activities.get(index - 1).getDurationMinutes();
  }

  private double itemValueAt(int index) {
    return activities.get(index - 1).getCombinedAverageRating();
  }
}
