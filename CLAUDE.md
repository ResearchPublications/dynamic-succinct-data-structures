# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a PhD thesis research project implementing experimental data structures and search algorithms. The research focuses on:
- **Chapter 3**: Elias-Fano compression for monotone integer sequences with dynamic variants
- **Chapter 4**: Branching bits analysis and fast search algorithms for small datasets

## Build and Run Commands

### Chapter 3: EliasFano (Java/Maven)

```bash
cd final_thesis_experiments-main/chp3/EliasFano

# Compile
mvn compile

# Package
mvn package

# Run tests
mvn test
```

### Chapter 4: BranchingBits (Java)

```bash
cd final_thesis_experiments-main/chp4/BranchingBits

# Compile and run
javac BranchingBits.java
java BranchingBits <size> <shift>

# Run full benchmark suite
./run.sh
```

### Chapter 4: FastInSmall (C++)

```bash
cd final_thesis_experiments-main/chp4/FastInSmall

# Compile with AVX2 optimizations
g++ -std=c++11 -march=native -mssse3 -mavx2 -mbmi2 -O3 All.cpp -o Demo

# Run with dataset size
./Demo <size>   # size: 4, 8, 16, 32, 64

# Run full benchmark suite
./run.sh
```

## Architecture

### Chapter 3: Elias-Fano Implementations

The core data structure hierarchy in `chp3/EliasFano/src/main/java/`:

- **Foundation classes** (`it/unipi/di/`): Abstract monotone sequence interfaces (LGPL licensed, from Giulio Ermanno Pibiri)
- **Custom implementations** (`com/chp3/efano/`):
  - `EliasFanoUniverseKnown` - Dynamic Elias-Fano when universe is known but size isn't
  - `EliasFanoBothUnknown` - Block-based (64 clusters) for unknown universe and size
  - `ClassicEliasFano` - Static version with selector indices
  - `ArraySearch` - Benchmarks for linear vs binary search variants
  - `DynamicArray<E>` / `ResizableBitVector` - Supporting data structures

Key dependencies: sux4j (succinct data structures), fastutil (efficient collections), dsiutils

### Chapter 4: Search Algorithm Implementations

**BranchingBits.java**: Analyzes bit-level decision points in monotone sequences using LCP (Longest Common Prefix) via XOR operations.

**All.cpp**: High-performance rank query implementations optimized for small datasets (<64 elements):
- `rankLinear` - Linear search baseline
- `rankBSI<N>` - Bit Slice Index variants (template specializations for sizes 4-512)
- `rankAFK` - Answer table + correction table approach
- `rankFWM` - Forward matching with SIMD predecessor search
- `rankSIMDXOR<N>` - SIMD-accelerated XOR-based ranking

Uses AVX2/SIMD intrinsics (`_mm256_*`) for vectorized operations.

## Benchmark Output

Results are stored as TSV files:
- `chp4/BranchingBits/results.tsv` - Branching bits distribution across sizes/shifts
- `chp4/FastInSmall/result.tsv` - Search algorithm timing comparisons
