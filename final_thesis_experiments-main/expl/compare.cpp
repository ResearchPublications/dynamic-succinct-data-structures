#include <iostream>
#include <vector>
#include <algorithm>
#include <chrono>
#include <fstream>
#include <string>
#include <sstream>
#include <unistd.h>
#include <iterator>

using namespace std;

// Function to get memory usage from /proc/self/stat
long getMemoryUsage() {
    std::ifstream stat_file("/proc/self/stat");
    std::string stat_line;
    if (stat_file.is_open()) {
        std::getline(stat_file, stat_line);
        stat_file.close();
    }
    
    std::istringstream iss(stat_line);
    std::vector<std::string> stats((std::istream_iterator<std::string>(iss)), std::istream_iterator<std::string>());
    if (stats.size() > 22) {
        return std::stol(stats[22]); // RSS (Resident Set Size) in pages
    }
    return 0;
}

// Linear search function
bool linearSearch(const std::vector<int>& data, int target) {
    for (int num : data) {
        if (num == target) {
            return true;
        }
    }
    return false;
}

// Binary search function
bool binarySearch(const std::vector<int>& data, int target) {
    int left = 0;
    int right = data.size() - 1;
    while (left <= right) {
        int middle = left + (right - left) / 2;
        if (data[middle] == target) {
            return true;
        }
        if (data[middle] < target) {
            left = middle + 1;
        } else {
            right = middle - 1;
        }
    }
    return false;
}

int main() {
    const int SIZE = 1000000; // Size of the dataset
    std::vector<int> data(SIZE);
    for (int i = 0; i < SIZE; ++i) {
        data[i] = i;
    }
    int target = SIZE - 1;

    // Measure time and memory for linear search
    auto start_time = std::chrono::high_resolution_clock::now();
    long memory_before = getMemoryUsage();
    bool linear_result = linearSearch(data, target);
    long memory_after = getMemoryUsage();
    auto end_time = std::chrono::high_resolution_clock::now();
    auto linear_duration = std::chrono::duration_cast<std::chrono::microseconds>(end_time - start_time).count();
    long linear_memory = memory_after - memory_before;

    // Measure time and memory for binary search
    std::sort(data.begin(), data.end()); // Ensure the data is sorted for binary search
    start_time = std::chrono::high_resolution_clock::now();
    memory_before = getMemoryUsage();
    bool binary_result = binarySearch(data, target);
    memory_after = getMemoryUsage();
    end_time = std::chrono::high_resolution_clock::now();
    auto binary_duration = std::chrono::duration_cast<std::chrono::microseconds>(end_time - start_time).count();
    long binary_memory = memory_after - memory_before;

    // Convert memory usage to bits
    long page_size = sysconf(_SC_PAGESIZE); // Get the size of a memory page in bytes
    long linear_memory_bits = linear_memory * page_size * 8;
    long binary_memory_bits = binary_memory * page_size * 8;

    // Output the results
    std::cout << "Linear Search: " << (linear_result ? "Found" : "Not Found") << "\n";
    std::cout << "Time taken: " << linear_duration << " microseconds\n";
    std::cout << "Memory used: " << linear_memory * page_size << " bytes (" << linear_memory_bits << " bits)\n\n";

    std::cout << "Binary Search: " << (binary_result ? "Found" : "Not Found") << "\n";
    std::cout << "Time taken: " << binary_duration << " microseconds\n";
    std::cout << "Memory used: " << binary_memory * page_size << " bytes (" << binary_memory_bits << " bits)\n"; 

    return 0;
}
