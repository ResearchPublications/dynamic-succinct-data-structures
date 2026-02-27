#!/bin/bash
# Comprehensive comparison benchmark: This work vs Dinklage et al.
# Must be run on Intel x86-64 with BMI2/AVX2 support

set -e

echo "=============================================="
echo "Predecessor Search Benchmark Comparison"
echo "This Work vs Dinklage et al. (SEA 2021)"
echo "=============================================="

# Check if running on x86_64
ARCH=$(uname -m)
if [[ "$ARCH" != "x86_64" ]]; then
    echo "ERROR: This benchmark requires x86_64 architecture (found: $ARCH)"
    echo "Please run on an Intel/AMD processor with BMI2 and AVX2 support."
    exit 1
fi

# Check for BMI2 support
if ! grep -q "bmi2" /proc/cpuinfo 2>/dev/null; then
    echo "WARNING: BMI2 instruction set not detected. Results may vary."
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TDC_DIR="$SCRIPT_DIR/tdc"
RESULTS_DIR="$SCRIPT_DIR/results"

mkdir -p "$RESULTS_DIR"

# Clone and build TDC if not present
if [ ! -d "$TDC_DIR" ]; then
    echo ""
    echo ">>> Cloning Dinklage's TDC library..."
    git clone -b sea21-predecessor https://github.com/pdinklag/tdc.git "$TDC_DIR"
fi

if [ ! -f "$TDC_DIR/build/benchmark/bench_predecessor_dynamic" ]; then
    echo ""
    echo ">>> Building TDC library..."
    cd "$TDC_DIR"
    mkdir -p build && cd build
    cmake -DCMAKE_BUILD_TYPE=Release ..
    make -j$(nproc)
    cd "$SCRIPT_DIR"
fi

# Run Thesis benchmarks
echo ""
echo ">>> Running This Work benchmarks..."
cd "$SCRIPT_DIR/.."
./run.sh > "$RESULTS_DIR/thesis_results.tsv"
echo "Thesis results saved to: $RESULTS_DIR/thesis_results.tsv"

# Run Dinklage benchmarks for matching set sizes
echo ""
echo ">>> Running Dinklage et al. benchmarks..."
DINKLAGE_BENCH="$TDC_DIR/build/benchmark/bench_predecessor_dynamic"
QUERIES=33554432  # 2^25 queries to match thesis experiments

echo -e "size\tDinklage_time_us" > "$RESULTS_DIR/dinklage_results.tsv"

for SIZE in 4 8 16 32 64 128 256; do
    echo "  Testing size=$SIZE..."
    # Run Dinklage benchmark and extract query time
    RESULT=$("$DINKLAGE_BENCH" basic -u 32 -n $SIZE -q $QUERIES -s 777 2>&1 | grep -i "query" | head -1)
    echo -e "$SIZE\t$RESULT" >> "$RESULTS_DIR/dinklage_results.tsv"
done

echo ""
echo "Dinklage results saved to: $RESULTS_DIR/dinklage_results.tsv"

# Generate comparison summary
echo ""
echo ">>> Generating comparison summary..."
echo "=============================================="
echo "BENCHMARK RESULTS SUMMARY"
echo "=============================================="
echo ""
echo "This Work (Thesis) Results:"
head -5 "$RESULTS_DIR/thesis_results.tsv"
echo "..."
echo ""
echo "Dinklage et al. Results:"
cat "$RESULTS_DIR/dinklage_results.tsv"
echo ""
echo "=============================================="
echo "Full results available in: $RESULTS_DIR/"
echo "=============================================="
