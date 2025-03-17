package com.asialocalguide.gateway.core.service.planning;

import com.asialocalguide.gateway.core.domain.planning.ActivityData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivitySchedulerWithRatingsTest {
    private static final int SLOTS_PER_DAY = 24;
    private static final int DAYS = 3;

    @Test
    void testEmptyInput() {
        ActivityData data = new ActivityData(
                new boolean[0][0][0],
                new String[0][0][0],
                new int[0],
                new int[0]
        );

        assertThatThrownBy(() -> ActivitySchedulerWithRatings.scheduleActivities(data)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSingleActivityScheduling() {
        // 1 activity available at slot 8 on day 0
        boolean[][][] availability = createAvailability(1, 1, new int[][]{{0, 8}});

        ActivityData data = new ActivityData(
                availability,
                // Add start time for day 0, time 14:00, slots start at 6:00
                createStartTimes(new int[][]{{0, 14}}),
                new int[]{5},
                new int[]{1}
        );

        boolean[][][] schedule = ActivitySchedulerWithRatings.scheduleActivities(data);
        assertTrue(schedule[0][0][8]);
        assertEquals(1, countScheduledActivities(schedule));
    }

    @Test
    void testTimeSlotConflictResolution() {
        // Two activities competing for same slot, only selects best rating
        boolean[][][] availability = createAvailability(2, 1, new int[][]{{0, 8}}, new int[][]{{0, 8}});

        ActivityData data = new ActivityData(
                availability,
                createStartTimes(new int[][]{{0, 14}}, new int[][]{{0, 14}}),
                new int[]{5, 3},
                new int[]{1, 1}
        );

        boolean[][][] schedule = ActivitySchedulerWithRatings.scheduleActivities(data);
        assertTrue(schedule[0][0][8]);
        assertEquals(5, calculateTotalRating(schedule, new int[]{5, 3}));
    }

    @Test
    void testDurationConflictResolution() {
        // Two activities with different starting times but duration overlaps, 3 hour gap between activities shoul dbe enforced
        boolean[][][] availability = createAvailability(2, 1, new int[][]{{0, 8}}, new int[][]{{0, 10}});

        ActivityData data = new ActivityData(
                availability,
                createStartTimes(new int[][]{{0, 14}}, new int[][]{{0, 14}}),
                new int[]{5, 3},
                new int[]{2, 2}
        );

        boolean[][][] schedule = ActivitySchedulerWithRatings.scheduleActivities(data);
        assertTrue(schedule[0][0][8]);
        assertEquals(5, calculateTotalRating(schedule, new int[]{5, 3}));
    }

    @Test
    void testNonConflictingActivities() {
        // Two activities in different slots
        // Slots 0 is 6am
        boolean[][][] availability = createAvailability(2, 1, new int[][]{{0, 3}}, new int[][]{{0, 10}});

        ActivityData data = new ActivityData(
                availability,
                createStartTimes(new int[][]{{0, 9}}, new int[][]{{0, 16}}),
                new int[]{5, 3},
                new int[]{1, 1}
        );

        boolean[][][] schedule = ActivitySchedulerWithRatings.scheduleActivities(data);
        assertTrue(schedule[0][0][3]);
        assertTrue(schedule[1][0][10]);
        assertEquals(8, calculateTotalRating(schedule, new int[]{5, 3}));
    }

    @Test
    void testMultiSlotActivities() {
        boolean[][][] availability = createAvailability(2, 1, new int[][]{{0, 2}}, new int[][]{{0, 11}});

        ActivityData data = new ActivityData(
                availability,
                // Slot 0 is 6am
                createStartTimes(new int[][]{{0, 8}}, new int[][]{{0, 17}}),
                new int[]{5, 5},
                new int[]{2, 2} // 2 slots for each activity
        );

        boolean[][][] schedule = ActivitySchedulerWithRatings.scheduleActivities(data);
        assertTrue(schedule[0][0][2]); // Should block for first activity
        assertTrue(schedule[1][0][11]); // Should block for second activity
    }

    @Test
    void testUnavailableActivities() {
        boolean[][][] availability = new boolean[1][DAYS][SLOTS_PER_DAY]; // All false

        ActivityData data = new ActivityData(
                availability,
                new String[1][DAYS][SLOTS_PER_DAY],
                new int[]{5},
                new int[]{1}
        );

        boolean[][][] schedule = ActivitySchedulerWithRatings.scheduleActivities(data);
        assertEquals(0, countScheduledActivities(schedule));
    }

    /**
     * Creates a 3D start times matrix for activities with formatted time strings.
     *
     * @param slotsPerActivity Variable arguments where each int[][] represents:
     *                         - For each activity: Array of {day, slot} pairs
     *                         - Each entry formats the slot number into "HH:00" time string
     * @return 3D String array of start times formatted as "HH:00"
     * Example: createStartTimes(new int[][]{{0,9}}, new int[][]{{0,15}})
     * Creates start times for:
     * - Activity 0: Day 0 at 09:00
     * - Activity 1: Day 0 at 15:00
     */
    private String[][][] createStartTimes(int[][]... slotsPerActivity) {
        String[][][] startTimes = new String[slotsPerActivity.length][DAYS][SLOTS_PER_DAY];

        for (int a = 0; a < slotsPerActivity.length; a++) {
            for (int[] daySlot : slotsPerActivity[a]) {
                int day = daySlot[0];
                int slot = daySlot[1];
                if (day < DAYS && slot < SLOTS_PER_DAY) {
                    startTimes[a][day][slot] = String.format("%02d:00", slot);
                }
            }
        }
        return startTimes;
    }

    /**
     * Constructs a 3D availability matrix marking available time slots for activities.
     *
     * @param numActivities        Total number of activities to create slots for
     * @param numDays              Number of days in the scheduling period
     * @param activityAvailability Variable arguments where each int[][] contains:
     *                             - For each activity: Array of {day, slot} pairs to mark as available
     * @return 3D boolean array where true indicates an available time slot
     * <p>
     * Example: createAvailability(2, 1, new int[][]{{0,8}}, new int[][]{{0,15}})
     * Creates availability for:
     * - Activity 0: Available on Day 0 at Slot 8 (08:00-09:00)
     * - Activity 1: Available on Day 0 at Slot 15 (15:00-16:00)
     */
    private static boolean[][][] createAvailability(int numActivities, int numDays, int[][]... activityAvailability) {
        boolean[][][] availability = new boolean[numActivities][numDays][SLOTS_PER_DAY];
        // Iterates over each activity's availability data.
        for (int a = 0; a < activityAvailability.length; a++) {
            // Iterates over the list of {day, slot} pairs for the current activity.
            for (int[] daySlot : activityAvailability[a]) {
                int day = daySlot[0];
                int slot = daySlot[1];
                if (day < numDays && slot < SLOTS_PER_DAY) {
                    // Marks the time slot as available (true) for the given activity.
                    availability[a][day][slot] = true;
                }
            }
        }
        return availability;
    }

    /**
     * Counts total number of scheduled time slots across all activities and days.
     *
     * @param schedule 3D boolean array from scheduler (activities x days x slots)
     * @return Total count of true values in the schedule matrix
     */
    private int countScheduledActivities(boolean[][][] schedule) {
        int count = 0;
        for (boolean[][] activity : schedule) {
            for (boolean[] day : activity) {
                for (boolean scheduled : day) {
                    if (scheduled) count++;
                }
            }
        }
        return count;
    }

    /**
     * Calculates the total rating score of all scheduled activities.
     *
     * @param schedule 3D boolean array from scheduler
     * @param ratings  Array of ratings indexed by activity
     * @return Sum of ratings for all scheduled activities
     * Note: Each activity's rating is added once per scheduled occurrence,
     * though typically activities should only be scheduled once
     */
    private int calculateTotalRating(boolean[][][] schedule, int[] ratings) {
        int total = 0;
        for (int a = 0; a < schedule.length; a++) {
            for (int d = 0; d < schedule[a].length; d++) {
                for (int t = 0; t < schedule[a][d].length; t++) {
                    if (schedule[a][d][t]) total += ratings[a];
                }
            }
        }
        return total;
    }


}