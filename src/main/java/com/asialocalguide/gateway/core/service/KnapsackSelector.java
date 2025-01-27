package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.dto.DayActivityDTO;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class KnapsackSelector {

  public List<DayActivityDTO> selectBestActivities(List<DayActivityDTO> activities, int capacity) {
    // Extract weights (durations) and values (ratings) from the activities
    int[] weights = activities.stream().mapToInt(DayActivityDTO::getDurationMinutes).toArray();
    double[] values =
        activities.stream().mapToDouble(DayActivityDTO::getCombinedAverageRating).toArray();

    // Solve the knapsack problem
    Set<Integer> selectedIndexes = solveKnapSack(capacity, weights, values, activities.size());

    // Map the selected indexes back to the original activities
    return selectedIndexes.stream().map(activities::get).toList();
  }

  private Set<Integer> solveKnapSack(int W, int[] weights, double[] values, int n) {
    int i;
    int w;
    double[][] maxValues = new double[n + 1][W + 1];

    for (i = 0; i <= n; i++) {
      for (w = 0; w <= W; w++) {
        if (i == 0 || w == 0) maxValues[i][w] = 0;
        else if (weights[i - 1] <= w)
          maxValues[i][w] =
              Math.max(values[i - 1] + maxValues[i - 1][w - weights[i - 1]], maxValues[i - 1][w]);
        else maxValues[i][w] = maxValues[i - 1][w];
      }
    }

    Set<Integer> selection = new HashSet<>();

    double res = maxValues[n][W];

    w = W;

    // Backtrack to find the items that are included in the knapsack
    for (i = n; i > 0 && res > 0; i--) {

      // either the result comes from the top
      // (K[i-1][w]) or from (val[i-1] + K[i-1]
      // [w-wt[i-1]]) as in Knapsack table. If
      // it comes from the latter one/ it means
      // the item is included.
      if (res == maxValues[i - 1][w]) {
        continue;
      } else {

        // This item was chosen for the knapsack.
        selection.add(i - 1);

        // Since this weight is included its
        // value is deducted
        res = res - values[i - 1];
        w = w - weights[i - 1];
      }
    }

    return selection;
  }
}
