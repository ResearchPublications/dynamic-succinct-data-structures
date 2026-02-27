package com.chp3.efano;

import java.text.DecimalFormat;

/**
 * This Benchmark tests the speed of linear search vs binary search on arrays of
 * small sizes. The idea is to identify the best algorithm to use depending on
 * the size of the arrays. It can be seen that linear search works better when
 * the size of the array is <= 256.
 *
 * The setup is as follows: create an array of given size between 32 to 2048 and
 * populate it with random numbers. We tried to make the width between the
 * numbers to be small so that search can be spread across the entire length of
 * the array. We then generated an array of search elements of length 2^25 and
 * then search within the array. We measured the time taken for various
 * implementations of Linear and Binary searches.
 */
public class ArraySearch {

    // Size of the array
    static int size;
    // Array to hold long values
    static long[] array;

    // Constructor to initialize the array with random increasing numbers
    public ArraySearch(int size) {
        array = GenerateData.generateIncreasingRandomLongs(size);
        this.size = array.length;
    }

    // Forward linear search
    public static int linearSearchFW(long element) {
        for (int i = 0; i < size; i++) {
            if (array[i] >= element) {
                return i;
            }
        }
        return -1;
    }

    // Linear search with sentinel
    public static int linearSentinel(long element) {
        int i = 0;
        while (true) {
            if (array[i++] >= element) {
                return i;
            }
        }
    }

    // Backward linear search
    public static int linearSearchBW(long element) {
        for (int i = size - 1; i > 0; i--) {
            if (array[i] <= element) {
                return i;
            }
        }
        return -1;
    }

    // Standard binary search
    public static int binarySearch(long key) {
        int low = 0, high = size, mid;
        while (low < high) {
            mid = (low + high) / 2;
            if (array[mid] == key) {
                return mid;
            } else if (array[mid] < key) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return (low < size) ? low : -1;
    }

    // Optimized binary search
    public static int binarySearchOptzd(long target) {
        int begin = 0, end = size - 1, mid;
        while (begin <= end) {
            mid = (begin + end) / 2;
            if (array[mid] <= target) {
                begin = mid + 1;
            } else {
                end = mid - 1;
            }
        }
        return begin;
    }

    // Get the maximum value in the array
    public long getMax() {
        return array[size - 1];
    }

    // Main method to run the benchmarks
    public static void main(String[] args) {
        int numLocs = (int) Math.pow(2, 25); // Number of locations to search
        DecimalFormat df = new DecimalFormat("###.###");
        System.err.println("Size, SEN, LFW, LBW, BSC, MBSC");

        // Loop through different sizes of the array
        for (int k = 5; k <= 10; k++) {
            int size = (int) Math.pow(2, k);
            ArraySearch obj = new ArraySearch(size);
            int[] loc = GenerateData.generateRandomIndexes(obj.getMax(), numLocs);

            System.out.print(size + ", ");

            // Benchmark for linearSentinel
            benchmarkSearch("linearSentinel", loc, df);

            // Benchmark for linearSearchFW
            benchmarkSearch("linearSearchFW", loc, df);

            // Benchmark for linearSearchBW
            benchmarkSearch("linearSearchBW", loc, df);

            // Benchmark for binarySearch
            benchmarkSearch("binarySearch", loc, df);

            // Benchmark for binarySearchOptzd
            benchmarkSearch("binarySearchOptzd", loc, df);

            System.out.print("\n");
        }
    }

    // Helper method to benchmark a specific search method
    private static void benchmarkSearch(String methodName, int[] loc, DecimalFormat df) {
        long sum = 0;
        double time = 0;

        for (int p = 0; p < 5; p++) {
            time = System.nanoTime();
            for (int i = 0; i < loc.length; i++) {
                try {
                    sum += (int) ArraySearch.class.getMethod(methodName, long.class).invoke(null, loc[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            time = (System.nanoTime() - time) / 1E9;
        }
        System.out.print(df.format(time) + ", ");
    }
}
