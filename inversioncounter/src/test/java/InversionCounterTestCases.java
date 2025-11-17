import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

import inversioncounter.InversionCounter;

public class InversionCounterTestCases {

    /**
     * Helper to create an array of size n in strictly decreasing order:
     * n, n-1, ..., 2, 1
     */
    private static int[] createDecreasingArray(int n) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = n - i;
        }
        return arr;
    }

    /* ======================= SLOW ALGORITHM TESTS ======================= */

    // Empty input array (required)
    @Test
    public void testCountInversionsSlowEmptyArray() {
        int[] arr = new int[0];
        long expected = 0L;

        long actual = InversionCounter.countInversionsSlow(arr);
        assertEquals(expected, actual);
    }

    // Simple sorted array with no inversions
    @Test
    public void testCountInversionsSlowSortedArray() {
        int[] arr = {1, 2, 3, 4, 5};
        long expected = 0L;

        long actual = InversionCounter.countInversionsSlow(arr);
        assertEquals(expected, actual);
    }

    // Typical small example with known inversion count
    @Test
    public void testCountInversionsSlowTypicalArray() {
        // Standard example: [2, 3, 8, 6, 1] → 5 inversions
        int[] arr = {2, 3, 8, 6, 1};
        long expected = 5L;

        long actual = InversionCounter.countInversionsSlow(arr);
        assertEquals(expected, actual);
    }

    // Test including both Integer.MIN_VALUE and Integer.MAX_VALUE (required)
    @Test
    public void testCountInversionsSlowWithMinAndMaxValues() {
        int[] arr = {Integer.MAX_VALUE, 0, Integer.MIN_VALUE};
        // Pairs:
        // (MAX, 0), (MAX, MIN), (0, MIN) → 3 inversions
        long expected = 3L;

        long actual = InversionCounter.countInversionsSlow(arr);
        assertEquals(expected, actual);
    }

    // Test input array of 100,000 integers in strictly decreasing order (required)
    @Test
    public void testCountInversionsSlowLargeDecreasingArray() {
        int n = 100_000;
        int[] arr = createDecreasingArray(n);
        long expected = (long) n * (n - 1) / 2; // n*(n-1)/2, must be long

        long actual = InversionCounter.countInversionsSlow(arr);
        assertEquals(expected, actual);
    }

    /* ======================= FAST ALGORITHM TESTS ======================= */

    // Empty input array (required)
    @Test
    public void testCountInversionsFastEmptyArray() {
        int[] arr = new int[0];
        long expected = 0L;

        long actual = InversionCounter.countInversionsFast(arr);
        assertEquals(expected, actual);
    }

    // Simple sorted array with no inversions
    @Test
    public void testCountInversionsFastSortedArray() {
        int[] arr = {1, 2, 3, 4, 5};
        long expected = 0L;

        long actual = InversionCounter.countInversionsFast(arr);
        assertEquals(expected, actual);
    }

    // Typical small example with known inversion count
    @Test
    public void testCountInversionsFastTypicalArray() {
        int[] arr = {2, 3, 8, 6, 1};
        long expected = 5L;

        long actual = InversionCounter.countInversionsFast(arr);
        assertEquals(expected, actual);
    }

    // Test including both Integer.MIN_VALUE and Integer.MAX_VALUE (required)
    @Test
    public void testCountInversionsFastWithMinAndMaxValues() {
        int[] arr = {Integer.MAX_VALUE, 0, Integer.MIN_VALUE};
        long expected = 3L;

        long actual = InversionCounter.countInversionsFast(arr);
        assertEquals(expected, actual);
    }

    // Test input array of 100,000 integers in strictly decreasing order (required)
    @Test
    public void testCountInversionsFastLargeDecreasingArray() {
        int n = 100_000;
        int[] arr = createDecreasingArray(n);
        long expected = (long) n * (n - 1) / 2;

        long actual = InversionCounter.countInversionsFast(arr);
        assertEquals(expected, actual);
    }
}
