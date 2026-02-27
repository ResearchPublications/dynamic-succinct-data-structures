package com.chp3.efano;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

/**
 * This class demonstrates the use of branching bits for processing binary
 * representations
 * of long values. It calculates the Longest Common Prefix (LCP) and uses it for
 * decision points.
 * The implementation includes methods for masking and binary string conversion.
 * 
 * The main method benchmarks the branching bits approach for various sizes of
 * input sets.
 * 
 * Author: ai146
 */
public class BranchingBits {

    // Method to generate a mask from decision points
    public static long mask(long[] decisionPoints) {
        long mask = 0;
        for (long i : decisionPoints) {
            mask |= 1 << (64 - i);
        }
        return mask;
    }

    // Get the Longest Common Prefix (LCP) between two long values using bit-level
    // operations and tzcnt
    public static int getLCP(long a, long b) {
        // XOR of a and b will have bits set where they differ
        long xor = a ^ b;
        // If a and b are the same, LCP is MaxBits
        if (xor == 0) {
            return 64;
        }
        // Find the position of the first differing bit (trailing zeros count)
        return 64 - Long.numberOfLeadingZeros(xor) - 1;
    }

    public static long[] genData(int length) {
        Random rand = new Random();
        long array[] = new long[length];
        array[0] = Long.MAX_VALUE % rand.nextLong();
        for (int j = 1; j < length; j++) {
            array[j] = array[j - 1] + Long.MAX_VALUE % rand.nextLong();
        }
        return array;
    }

    public static long[] shiftData(long[] array, int length, int shift) {
        for (int j = 0; j < length; j++) {
            array[j] = array[j] + shift;
        }
        return array;
    }

    // Main method to benchmark the branching bits approach
    public static void main(String[] args) {

        Random rand = new Random();

        System.out.println("Size\tBranching Bits");
        // Loop for benchmarking with different input sizes
        for (int size = 16; size <= 1024; size *= 2) {
            System.out.print(size + ", ");
            long[] array = BranchingBits.genData(size);
            HashSet<Integer> nbb = new HashSet<>();

            for (int j = 0; j < 16; j++) {
                array = BranchingBits.shiftData(array, size, rand.nextInt());
                for (int i = 1; i < size; i++) {
                    int lcp = BranchingBits.getLCP(array[i - 1], array[i]);
                    nbb.add(lcp);
                }
                System.out.print(nbb.size() + "\t");
                nbb.clear();
            }
            System.out.println();
        }
    }
}
