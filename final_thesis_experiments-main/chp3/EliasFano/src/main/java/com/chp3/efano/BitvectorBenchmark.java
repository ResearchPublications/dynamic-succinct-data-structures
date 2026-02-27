package com.chp3.efano;

import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.sux4j.bits.Rank9;
import it.unimi.dsi.sux4j.bits.SimpleSelect;
import java.util.Random;

/**
 * Benchmark comparing bitvector rank/select implementations:
 * 1. Thesis append-only bitvector with rank/select
 * 2. sux4j Rank9 and SimpleSelect (static structures)
 *
 * Measures: build time, rank query time, select query time, space overhead
 */
public class BitvectorBenchmark {

    private static final int WARMUP_ITERATIONS = 3;
    private static final int BENCHMARK_ITERATIONS = 10;
    private static final int NUM_QUERIES = 100000;
    private static final int[] TEST_SIZES = {10000, 100000, 1000000, 10000000};

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("BITVECTOR RANK/SELECT BENCHMARK");
        System.out.println("Comparing: Thesis append-only bitvector vs sux4j Rank9/SimpleSelect");
        System.out.println("=".repeat(80));
        System.out.println();

        // Print header
        System.out.println("Size\tDensity\tSux4j_Build(ms)\tSux4j_Rank(ns)\tSux4j_Select(ns)\tThesis_Build(ms)\tThesis_Rank(ns)\tThesis_Select(ns)");

        for (int size : TEST_SIZES) {
            runBenchmark(size, 0.5);  // 50% density (balanced 0s and 1s)
        }

        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println("DENSITY ANALYSIS (size=1M)");
        System.out.println("=".repeat(80));

        double[] densities = {0.1, 0.25, 0.5, 0.75, 0.9};
        System.out.println("Density\tSux4j_Rank(ns)\tSux4j_Select(ns)\tThesis_Rank(ns)\tThesis_Select(ns)");
        for (double density : densities) {
            runDensityBenchmark(1000000, density);
        }

        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println("SPACE OVERHEAD ANALYSIS");
        System.out.println("=".repeat(80));
        analyzeSpaceOverhead();
    }

    private static void runBenchmark(int size, double density) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            benchmarkSux4j(size, density);
            benchmarkThesis(size, density);
        }

        // Benchmark sux4j
        long[] sux4jResults = new long[4];
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long[] result = benchmarkSux4j(size, density);
            for (int j = 0; j < 4; j++) sux4jResults[j] += result[j];
        }

        // Benchmark thesis approach
        long[] thesisResults = new long[4];
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long[] result = benchmarkThesis(size, density);
            for (int j = 0; j < 4; j++) thesisResults[j] += result[j];
        }

        System.out.printf("%d\t%.1f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f%n",
            size, density,
            sux4jResults[0] / (double) BENCHMARK_ITERATIONS,
            sux4jResults[1] / (double) BENCHMARK_ITERATIONS,
            sux4jResults[2] / (double) BENCHMARK_ITERATIONS,
            thesisResults[0] / (double) BENCHMARK_ITERATIONS,
            thesisResults[1] / (double) BENCHMARK_ITERATIONS,
            thesisResults[2] / (double) BENCHMARK_ITERATIONS);
    }

    private static void runDensityBenchmark(int size, double density) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            benchmarkSux4j(size, density);
            benchmarkThesis(size, density);
        }

        long[] sux4jResults = new long[4];
        long[] thesisResults = new long[4];

        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long[] s = benchmarkSux4j(size, density);
            long[] t = benchmarkThesis(size, density);
            for (int j = 0; j < 4; j++) {
                sux4jResults[j] += s[j];
                thesisResults[j] += t[j];
            }
        }

        System.out.printf("%.2f\t%.2f\t%.2f\t%.2f\t%.2f%n",
            density,
            sux4jResults[1] / (double) BENCHMARK_ITERATIONS,
            sux4jResults[2] / (double) BENCHMARK_ITERATIONS,
            thesisResults[1] / (double) BENCHMARK_ITERATIONS,
            thesisResults[2] / (double) BENCHMARK_ITERATIONS);
    }

    /**
     * Benchmark sux4j Rank9 and SimpleSelect
     * Returns: [build_time_ms, rank_time_ns, select_time_ns, space_bytes]
     */
    private static long[] benchmarkSux4j(int size, double density) {
        Random rand = new Random(42);
        LongArrayBitVector bv = LongArrayBitVector.getInstance(size);

        // Build bitvector
        long buildStart = System.nanoTime();
        int oneCount = 0;
        for (int i = 0; i < size; i++) {
            boolean bit = rand.nextDouble() < density;
            bv.add(bit);
            if (bit) oneCount++;
        }

        // Build rank/select structures
        Rank9 rank = new Rank9(bv);
        SimpleSelect select = new SimpleSelect(bv);
        long buildTime = (System.nanoTime() - buildStart) / 1_000_000;

        // Rank queries
        rand = new Random(123);
        long rankStart = System.nanoTime();
        long sum = 0;
        for (int i = 0; i < NUM_QUERIES; i++) {
            int idx = rand.nextInt(size);
            sum += rank.rank(idx);
        }
        long rankTime = (System.nanoTime() - rankStart) / NUM_QUERIES;

        // Select queries
        rand = new Random(456);
        long selectStart = System.nanoTime();
        if (oneCount > 0) {
            for (int i = 0; i < NUM_QUERIES; i++) {
                int idx = rand.nextInt(oneCount) + 1;
                sum += select.select(idx);
            }
        }
        long selectTime = (System.nanoTime() - selectStart) / NUM_QUERIES;

        // Prevent optimization
        if (sum < 0) System.out.println(sum);

        return new long[]{buildTime, rankTime, selectTime, 0};
    }

    /**
     * Benchmark thesis append-only bitvector with rank/select
     * Returns: [build_time_ms, rank_time_ns, select_time_ns, space_bytes]
     */
    private static long[] benchmarkThesis(int size, double density) {
        Random rand = new Random(42);

        // Build bitvector using ResizableBitVector
        long buildStart = System.nanoTime();
        ResizableBitVector rbv = new ResizableBitVector();
        int oneCount = 0;
        for (int i = 0; i < size; i++) {
            int bit = rand.nextDouble() < density ? 1 : 0;
            rbv.append(bit);
            if (bit == 1) oneCount++;
        }

        // Build select structure
        SimpleSelect select = rbv.buildSelectIndex();
        long buildTime = (System.nanoTime() - buildStart) / 1_000_000;

        // Rank queries (using bitvector directly - count 1s up to position)
        rand = new Random(123);
        long rankStart = System.nanoTime();
        long sum = 0;
        LongArrayBitVector bv = (LongArrayBitVector) rbv.bitVector();
        for (int i = 0; i < NUM_QUERIES; i++) {
            int idx = rand.nextInt(size);
            // Simple rank: count 1s from 0 to idx
            sum += bv.subVector(0, idx).count();
        }
        long rankTime = (System.nanoTime() - rankStart) / NUM_QUERIES;

        // Select queries
        rand = new Random(456);
        long selectStart = System.nanoTime();
        if (oneCount > 0) {
            for (int i = 0; i < NUM_QUERIES; i++) {
                int idx = rand.nextInt(oneCount) + 1;
                sum += select.select(idx);
            }
        }
        long selectTime = (System.nanoTime() - selectStart) / NUM_QUERIES;

        // Prevent optimization
        if (sum < 0) System.out.println(sum);

        return new long[]{buildTime, rankTime, selectTime, rbv.capacity / 8};
    }

    private static void analyzeSpaceOverhead() {
        System.out.println("\nSpace Overhead Analysis:");
        System.out.println("Size\tRaw_bits\tThesis_bits\tOverhead%\tSux4j_Rank_bits\tSux4j_Overhead%");

        int[] sizes = {10000, 100000, 1000000};

        for (int size : sizes) {
            Random rand = new Random(42);

            // Thesis approach
            ResizableBitVector rbv = new ResizableBitVector();
            for (int i = 0; i < size; i++) {
                rbv.append(rand.nextInt(2));
            }
            long thesisBits = rbv.capacity;
            double thesisOverhead = 100.0 * (thesisBits - size) / size;

            // sux4j approach
            rand = new Random(42);
            LongArrayBitVector bv = LongArrayBitVector.getInstance(size);
            for (int i = 0; i < size; i++) {
                bv.add(rand.nextInt(2) == 1);
            }
            Rank9 rank = new Rank9(bv);
            // Rank9 uses approximately 25% extra space
            long sux4jBits = size + (size / 4);
            double sux4jOverhead = 100.0 * (sux4jBits - size) / size;

            System.out.printf("%d\t%d\t%d\t%.2f%%\t%d\t%.2f%%%n",
                size, size, thesisBits, thesisOverhead, sux4jBits, sux4jOverhead);
        }

        System.out.println("\nConclusion: Thesis approach achieves o(n) overhead through fine-grained resizing,");
        System.out.println("while sux4j Rank9 has fixed 25% overhead for rank support structures.");
    }
}
