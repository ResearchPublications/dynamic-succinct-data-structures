# Dinklage et al. Comparison Benchmark

This directory contains scripts and code to benchmark the thesis implementations against Dinklage et al.'s predecessor data structures from their SEA 2021 paper:

> P. Dinklage, J. Fischer, A. Herlez. "Engineering Predecessor Data Structures for Dynamic Integer Sets". SEA 2021.

## Requirements

- Linux with GCC 9.3+ (C++17 support required)
- CPU with BMI2 instruction set support (Intel Haswell or newer)
- CMake 3.10+
- AVX2 support

**Note**: These benchmarks must be run on Intel x86-64 hardware. ARM-based systems (including Apple Silicon) are not supported.

## Setup

### 1. Clone Dinklage's TDC Library

```bash
git clone -b sea21-predecessor https://github.com/pdinklag/tdc.git
cd tdc
mkdir build && cd build
cmake -DCMAKE_BUILD_TYPE=Release ..
make
```

### 2. Run Dinklage's Benchmark

```bash
# Test with small set sizes (matching our thesis experiments)
# -u 32: 32-bit universe
# -n: number of keys
# -q: number of queries (same as our 2^25 queries)
./benchmark/bench_predecessor_dynamic basic -u 32 -n 4 -q 33554432 -s 777
./benchmark/bench_predecessor_dynamic basic -u 32 -n 8 -q 33554432 -s 777
./benchmark/bench_predecessor_dynamic basic -u 32 -n 16 -q 33554432 -s 777
./benchmark/bench_predecessor_dynamic basic -u 32 -n 32 -q 33554432 -s 777
./benchmark/bench_predecessor_dynamic basic -u 32 -n 64 -q 33554432 -s 777
./benchmark/bench_predecessor_dynamic basic -u 32 -n 128 -q 33554432 -s 777
./benchmark/bench_predecessor_dynamic basic -u 32 -n 256 -q 33554432 -s 777
```

### 3. Run Thesis Implementation Benchmark

```bash
cd ..  # back to FastInSmall directory
./run.sh > thesis_results.tsv
```

## Data Structures Compared

### This Thesis (Static Small Sets)
| Method | Query Complexity | Space | Notes |
|--------|-----------------|-------|-------|
| Ajtai (rankAFK) | O(1) | O(2^b) | Fastest query, highest space |
| Improved Fredman (rankFWMI) | O(1) | O(k) | Fast query, minimal space |
| Branchless Binary Search (rankBSI) | O(log k) | O(1) | Baseline |
| std::lower_bound (rankSTL) | O(log k) | O(1) | STL baseline |

### Dinklage et al. (Dynamic Sets)
| Method | Query Complexity | Insert/Delete | Notes |
|--------|-----------------|---------------|-------|
| B-tree (B=64) | O(log n / log B) | O(log n / log B) | Best overall in their experiments |
| Y-fast trie | O(log log u) | O(log log u) | Space-efficient |
| Fusion tree | O(log_w n) | O(log_w n) | Theoretical optimal |

## Expected Results

For small static sets (k ≤ 256), our O(1) implementations should outperform Dinklage's structures which are optimized for dynamic larger sets. The crossover point where Dinklage's structures become competitive is expected around k > 512.

## References

- Paper: https://drops.dagstuhl.de/entities/document/10.4230/LIPIcs.SEA.2021.7
- Code: https://github.com/pdinklag/tdc/tree/sea21-predecessor
- arXiv: https://arxiv.org/abs/2104.06740
