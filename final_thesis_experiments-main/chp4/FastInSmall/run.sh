#!/bin/bash
# Benchmark script for predecessor search implementations
# Columns: size, NBB (significant bit positions), timings in microseconds
echo -e "size\tNBB\ttLIN\ttSTL\ttAFK\ttFWM\ttFWMI\ttBSI\tCheck"
g++ -std=c++11 -march=native -mssse3 -mavx2 -mbmi2 -O3 All.cpp -o Demo
n=20
for((i = 1; i <= n; i++)) do
	./Demo 4
done
for((i = 1; i <= n; i++)) do
	./Demo 8
done
for((i = 1; i <= n; i++)) do
	./Demo 16
done
for((i = 1; i <= n; i++)) do
	./Demo 32
done
for((i = 1; i <= n; i++)) do
	./Demo 64
done
for((i = 1; i <= n; i++)) do
	./Demo 128
done
for((i = 1; i <= n; i++)) do
	./Demo 256
done
rm Demo