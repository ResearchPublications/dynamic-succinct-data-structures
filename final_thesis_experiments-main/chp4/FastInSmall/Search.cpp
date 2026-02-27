#include <bits/stdc++.h>
#include <immintrin.h>
#include <stdint.h>
#include <stdlib.h>
#include <algorithm>
#include <cassert>
#include <cmath>
#include <ctime>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <limits>
#include <random>
#include <stack>
#include <string>
#include <vector>

#pragma GCC target("avx2")

using namespace std;

class All {
   public:
    All(uint64_t *inpt, unsigned int size);
    void insert(uint64_t value);
    int blindSearch(uint64_t value);
    int getNBBCount(void);
    uint64_t rankAFK(uint64_t val);
    uint64_t rankFWM(uint64_t val);
    uint64_t predSearch(uint64_t val);
    uint64_t predSearch2(uint64_t val);
    uint64_t rankLinear(uint64_t val);
    uint64_t rankBinary(uint64_t val);

    template <int size>
    uint64_t rankBSI(uint64_t val);

    template <int size>
    uint64_t rankSIMDXOR(uint64_t key);

    uint64_t rankBSI4(uint64_t val);
    uint64_t rankBSI8(uint64_t val);
    uint64_t rankBSI16(uint64_t val);
    uint64_t rankBSI32(uint64_t val);
    uint64_t rankBSI64(uint64_t val);
    uint64_t rankBSI128(uint64_t val);
    uint64_t rankBSI256(uint64_t val);
    uint64_t rankBSI512(uint64_t val);
    uint64_t getLCP(uint64_t x, uint64_t y);
    uint64_t getLCPFW(uint64_t x, uint64_t y);
    uint16_t extractBits(uint64_t val);
    bool membership(uint64_t i);
    void print(void);
    uint8_t _mm_movemask_epi16(__m128i i);
    int minSIMD8(__m256i i);
    uint16_t *extractedinput;
    uint64_t *decPointRank;
    uint64_t **correctionTable;
    uint64_t **correctionTableFW;
    unsigned int maxbits;

   private:
    class Node {
       public:
        Node(int index, int position, string label, Node *left, Node *right,
             bool terminate) {
            this->index = index;
            this->pos = position;
            this->label = label;
            this->left = left;
            this->right = right;
            this->terminate = terminate;
        }

        Node(int index, int position, string label, bool terminate) {
            this->index = index;
            this->pos = position;
            this->label = label;
            this->terminate = terminate;
        }

        Node(void) {
            this->index = 0;
            this->pos = -1;
            this->label = "root";
            this->terminate = false;
        }

        Node *left;
        Node *right;
        int index;
        int pos;
        string label;
        bool terminate;
    };

    Node *root;
    unsigned int size;
    int nodeSize;
    int last, index = -1;
    uint64_t *answerTable;
    uint64_t *input;
    vector<int> decPoints;
    unsigned int mask;
    int ansSize;

    bool insert(Node *node, string value, int offset);
    string toBinaryString(uint64_t i);
    string toBinaryStringNBB(uint64_t i);
    void print(Node *n);
    void buildCorrectionTable();
    void buildCorrectionTableFW();
    int getLeftMost(Node *n);
    int getRightMost(Node *n);
};

All::All(uint64_t *in, unsigned int siz) {
    root = new Node();
    input = in;
    size = siz;
    maxbits = ceil(log2(in[size - 1]));
    last = 0;
    mask = 0;
    for (int i = 1; i < size; i++) {
        int lcp = getLCP(input[i - 1], input[i]);
        if (std::find(decPoints.begin(), decPoints.end(), lcp) ==
            decPoints.end()) {
            decPoints.push_back(lcp);
            mask = mask | (1 << (maxbits - decPoints.back() - 1));
        }
    }

    sort(decPoints.begin(), decPoints.end());

    ansSize = (int)pow(2, decPoints.size());
    answerTable = new uint64_t[ansSize];
    decPointRank = new uint64_t[maxbits];
    correctionTable = new uint64_t *[size];
    correctionTableFW = new uint64_t *[size];
    extractedinput = new uint16_t[size];

    for (int i = 0; i < size; i++) {
        insert(input[i]);
        extractedinput[i] = extractBits(input[i]);
    }

    for (int i = 0; i < ansSize; i++) {
        answerTable[i] = blindSearch(i);
    }

    for (int i = 0; i < maxbits; i++) {
        decPointRank[i] = predSearch2(i);
    }

    buildCorrectionTable();
    buildCorrectionTableFW();
}

uint8_t All::_mm_movemask_epi16(__m128i i) {
    return _mm_movemask_epi8(
        _mm_shuffle_epi8(i, _mm_setr_epi8(0, 2, 4, 6, 8, 10, 12, 14, -1, -1, -1,
                                          -1, -1, -1, -1, -1)));
}

uint64_t All::predSearch(uint64_t val) {
    __m256i a = _mm256_set1_epi16(val);
    __m256i b = _mm256_loadu_si256((__m256i *)&extractedinput[0]);
    __m256i c = _mm256_cmpeq_epi16(_mm256_max_epu16(b, a), b);
    uint64_t mask = _mm_movemask_epi16(_mm256_extracti128_si256(c, 1));
    mask = (mask << 8) | _mm_movemask_epi16(_mm256_extracti128_si256(c, 0));
    return mask == 0 ? size - 1 : __builtin_ctzll(mask);
}

uint64_t All::predSearch2(uint64_t val) {
    for (int i = 0; i < decPoints.size(); i++)
        if (decPoints[i] >= val) return i;
    return decPoints.size() - 1;
}

bool All::membership(uint64_t i) { return i == rankAFK(i); }

uint64_t All::getLCP(uint64_t x, uint64_t y) {
    return maxbits - (64 - __builtin_clzll(x ^ y));
}

uint64_t All::getLCPFW(uint64_t x, uint64_t y) {
    return maxbits - (64 - __builtin_clzll(x ^ y));
}

uint16_t All::extractBits(uint64_t val) { return _pext_u64(val, mask); }

uint64_t All::rankAFK(uint64_t val) {
    uint64_t eVal = extractBits(val);
    uint64_t a = answerTable[eVal];
    uint64_t b = getLCP(input[a], val);
    return correctionTable[a][b];
}

uint64_t All::rankFWM(uint64_t val) {
    uint64_t eVal = extractBits(val);
    uint64_t a = predSearch(eVal);
    uint64_t l = getLCP(input[a > 0 ? a - 1 : a], val);
    uint64_t c = getLCP(input[a], val);
    uint64_t r = getLCP(input[a + 1], val);
    if ((a > 0) && (c < l)) a -= 1;
    if ((a > 0) && (c < r)) a += 1;
    
    uint64_t b = getLCP(input[a], val);
    return correctionTable[a][b];
}

uint64_t All::rankLinear(uint64_t val) {
    uint64_t res = 0;
    for (int j = 0; j < size; j++) {
        if (input[j] >= val) return j;
    }
    return size - 1;
}

int All::minSIMD8(__m256i vec) {
  __m128i lo = _mm256_castsi256_si128(vec);
  __m128i hi = _mm256_extractf128_si256(vec, 1);
  __m128i tmp = _mm_min_epu32(lo, hi);
  tmp = _mm_min_epu32(tmp, _mm_shuffle_epi32(tmp, _MM_SHUFFLE(1, 0, 3, 2)));
  tmp = _mm_min_epu32(tmp, _mm_shufflelo_epi16(tmp, _MM_SHUFFLE(1, 0, 3, 2)));
  const int max = _mm_cvtsi128_si32(tmp);

  tmp = _mm_packs_epi16(_mm_packs_epi32(_mm_cmpeq_epi32(_mm_set1_epi32(max), lo), _mm_cmpeq_epi32(_mm_set1_epi32(max), hi)), _mm_setzero_si128());
  return _bit_scan_forward(_mm_movemask_epi8(tmp));
}

template <>
uint64_t All::rankBSI<4>(uint64_t val) {
    uint64_t i = (input[2] >= val) ? 0 : 2;
    i += (input[i + 1] >= val) ? 0 : 1;
    i += (input[i + 0] >= val) ? 0 : 1;
    return i;
}
template <>
uint64_t All::rankBSI<8>(uint64_t val) {
    uint64_t i = (input[4] >= val) ? 0 : 4;
    i += (input[i + 2] >= val) ? 0 : 2;
    i += (input[i + 1] >= val) ? 0 : 1;
    i += (input[i + 0] >= val) ? 0 : 1;
    return i;
}
template <>
uint64_t All::rankBSI<16>(uint64_t val) {
    uint64_t i = (input[8] >= val) ? 0 : 8;
    i += (input[i + 4] >= val) ? 0 : 4;
    i += (input[i + 2] >= val) ? 0 : 2;
    i += (input[i + 1] >= val) ? 0 : 1;
    i += (input[i + 0] >= val) ? 0 : 1;
    return i;
}

template <>
uint64_t All::rankBSI<32>(uint64_t val) {
    uint64_t i = (input[16] >= val) ? 0 : 16;
    i += (input[i + 8] >= val) ? 0 : 8;
    i += (input[i + 4] >= val) ? 0 : 4;
    i += (input[i + 2] >= val) ? 0 : 2;
    i += (input[i + 1] >= val) ? 0 : 1;
    i += (input[i + 0] >= val) ? 0 : 1;
    return i;
}

template <>
uint64_t All::rankBSI<64>(uint64_t val) {
    uint64_t i = (input[32] >= val) ? 0 : 32;
    i += (input[i + 16] >= val) ? 0 : 16;
    i += (input[i + 8] >= val) ? 0 : 8;
    i += (input[i + 4] >= val) ? 0 : 4;
    i += (input[i + 2] >= val) ? 0 : 2;
    i += (input[i + 1] >= val) ? 0 : 1;
    i += (input[i + 0] >= val) ? 0 : 1;
    return i;
}
template <>
uint64_t All::rankBSI<128>(uint64_t val) {
    uint64_t i = (input[64] >= val) ? 0 : 64;
    i += (input[i + 32] >= val) ? 0 : 32;
    i += (input[i + 16] >= val) ? 0 : 16;
    i += (input[i + 8] >= val) ? 0 : 8;
    i += (input[i + 4] >= val) ? 0 : 4;
    i += (input[i + 2] >= val) ? 0 : 2;
    i += (input[i + 1] >= val) ? 0 : 1;
    i += (input[i + 0] >= val) ? 0 : 1;
    return i;
}
template <>
uint64_t All::rankBSI<256>(uint64_t val) {
    uint64_t i = (input[128] >= val) ? 0 : 128;
    i += (input[i + 64] >= val) ? 0 : 64;
    i += (input[i + 32] >= val) ? 0 : 32;
    i += (input[i + 16] >= val) ? 0 : 16;
    i += (input[i + 8] >= val) ? 0 : 8;
    i += (input[i + 4] >= val) ? 0 : 4;
    i += (input[i + 2] >= val) ? 0 : 2;
    i += (input[i + 1] >= val) ? 0 : 1;
    i += (input[i + 0] >= val) ? 0 : 1;
    return i;
}

template <>
uint64_t All::rankBSI<512>(uint64_t val) {
    uint64_t i = (input[256] >= val) ? 0 : 256;
    i += (input[i + 128] >= val) ? 0 : 128;
    i += (input[i + 64] >= val) ? 0 : 64;
    i += (input[i + 32] >= val) ? 0 : 32;
    i += (input[i + 16] >= val) ? 0 : 16;
    i += (input[i + 8] >= val) ? 0 : 8;
    i += (input[i + 4] >= val) ? 0 : 4;
    i += (input[i + 2] >= val) ? 0 : 2;
    i += (input[i + 1] >= val) ? 0 : 1;
    i += (input[i + 0] >= val) ? 0 : 1;
    return i;
}

template <>
uint64_t All::rankSIMDXOR<4>(uint64_t val) {
  __m256i a = _mm256_set1_epi64x(val);
  __m256i b = _mm256_loadu_si256((__m256i *)&extractedinput[0]);
  __m256i xor_val = _mm256_xor_si256(a, b);
  return minSIMD8(xor_val);
}

template <>
uint64_t All::rankSIMDXOR<8>(uint64_t val) {
  __m256i a = _mm256_set1_epi32(val);
  __m256i b = _mm256_loadu_si256((__m256i *)&extractedinput[0]);
  __m256i xor_val = _mm256_xor_si256(a, b);;
  return minSIMD8(xor_val);
}

template <>
uint64_t All::rankSIMDXOR<16>(uint64_t val) {
  __m256i a = _mm256_set1_epi32(val);
  __m256i b = _mm256_loadu_si256((__m256i *)&extractedinput[0]);
  __m256i xor_val = _mm256_xor_si256(a, b);
  int left = minSIMD8(xor_val);

  b = _mm256_loadu_si256((__m256i *)&extractedinput[8]);
  xor_val = _mm256_xor_si256(a, b);
  int right = 8 + minSIMD8(xor_val);

  return (input[left] < input[right]) ? left : right;
}

template <>
uint64_t All::rankSIMDXOR<32>(uint64_t val) {
  __m256i a = _mm256_set1_epi32(val);
  __m256i b = _mm256_loadu_si256((__m256i *)&extractedinput[0]);
  __m256i xor_val = _mm256_xor_si256(a, b);
  int left = minSIMD8(xor_val);

  b = _mm256_loadu_si256((__m256i *)&extractedinput[8]);
  xor_val = _mm256_xor_si256(a, b);
  int right = 8 + minSIMD8(xor_val);

  int l = (input[left] < input[right]) ? left : right;

  b = _mm256_loadu_si256((__m256i *)&extractedinput[16]);
  xor_val = _mm256_xor_si256(a, b);
  left = 16 + minSIMD8(xor_val);

  b = _mm256_loadu_si256((__m256i *)&extractedinput[24]);
  xor_val = _mm256_xor_si256(a, b);
  right = 24 + minSIMD8(xor_val);

  int r = (input[left] < input[right]) ? left : right;

  return (input[l] < input[r]) ? l : r;
}


void All::insert(uint64_t value) {
    string text = toBinaryString(value);
    insert(root, text, 0);
    index++;
}

int All::blindSearch(uint64_t i) {
    string text = toBinaryStringNBB(i);
    Node *node = root;
    int p = 0;
    string label = "";
    int len = 0;
    while (p < text.size() && !node->terminate) {
        if (text.at(p) == '1') {
            node = node->right;
        } else {
            node = node->left;
        }
        label += node->label;
        len = label.size();
        if ((p + 1) < text.size()) {
            p += 1;
            while (p < text.size() && decPoints[p] < len) {
                int next = decPoints[p];
                char tc = text.at(p);
                char lc = label.at(next);
                if (tc != lc) {
                    if (tc == '0') {
                        return getLeftMost(node);
                    } else {
                        return getRightMost(node);
                    }
                } else {
                    p += 1;
                }
            }
        } else {
            return node->index;
        }
    }
    return node->index;
}

int All::getLeftMost(Node *n) {
    Node *node = n;
    while (!node->terminate) {
        node = node->left;
    }
    return node->index;
}

int All::getRightMost(Node *n) {
    Node *node = n;
    while (!node->terminate) {
        node = node->right;
    }
    return node->index;
}

bool All::insert(Node *node, string letters, int offset) {
    int lettersRest = letters.size() - offset;
    while (true) {
        int thisLettersLength = node->label.size();
        int n = min(lettersRest, thisLettersLength);
        int i = 0;
        while (i < n && (letters.at(i + offset) - node->label.at(i)) == 0) {
            i++;
        }
        if (i != n) {
            Node *child1 = new Node(index, i, node->label.substr(i), node->left,
                                    node->right, node->terminate);
            Node *child2 = new Node(index + 1, (i + offset),
                                    letters.substr(i + offset), true);
            node->index = i + 1;
            node->label = node->label.substr(0, i);
            node->terminate = false;
            if (child1->label.at(0) < child2->label.at(0)) {
                node->left = child1;
                node->right = child2;
            } else {
                node->left = child2;
                node->right = child1;
            }
            nodeSize += 2;
            return child1;
        } else if (lettersRest == thisLettersLength) {
            if (!node->terminate) {
                node->terminate = true;
                index++;
            }
            return node;
        } else if (lettersRest < thisLettersLength) {
            Node *newChild =
                new Node(index, lettersRest,
                         node->label.substr(lettersRest, thisLettersLength),
                         node->left, node->right, node->terminate);
            node->label = node->label.substr(0, i);
            node->terminate = true;
            if (node->left != NULL) {
                node->left = newChild;
            } else {
                node->right = newChild;
            }
            nodeSize++;
            return newChild;
        } else {
            int index = 0;
            int end = 0;
            if (node->left != NULL) end++;
            if (node->right != NULL) end++;

            bool cont = false;
            if (end > 16) {
                int start = 0;
                while (start < end) {
                    index = (start + end) / 2;
                    Node *child = node->left;
                    if (index == 1) child = node->right;

                    int c = letters.at(i + offset) - child->label.at(0);
                    if (c == 0) {
                        node = child;
                        offset += i;
                        lettersRest -= i;
                        cont = true;
                        break;
                    }
                    if (c < 0) {
                        end = index;
                    } else if (start == index) {
                        index = end;
                        break;
                    } else {
                        start = index;
                    }
                }
            } else {
                for (; index < end; index++) {
                    Node *child = node->left;
                    if (index == 1) child = node->right;
                    int c = letters.at(i + offset) - child->label.at(0);
                    if (c < 0) break;
                    if (c == 0) {
                        node = child;
                        offset += i;
                        lettersRest -= i;
                        cont = true;
                        break;
                    }
                }
            }
            if (cont) continue;
            Node *child = new Node(index, (i + offset) + 2,
                                   letters.substr(i + offset), true);
            if (index == 0)
                node->left = child;
            else
                node->right = child;

            nodeSize++;
            return child;
        }
    }
}

void All::buildCorrectionTable() {
    for (int i = 0; i < size; i++) {
        correctionTable[i] = new uint64_t[maxbits + 1];
        uint64_t elem = input[i];
        correctionTable[i][maxbits] = i;
        for (int j = 0; j < maxbits; j++) {
            uint64_t m = elem ^ (1 << j);
            correctionTable[i][maxbits - j - 1] = rankLinear(m);
        }
    }
}

void All::buildCorrectionTableFW() {
    for (int i = 0; i < size; i++) {
        correctionTableFW[i] = new uint64_t[decPoints.size() + 1];
        uint64_t elem = input[i];
        correctionTableFW[i][decPoints.size()] = i;
        for (int j = 0; j < decPoints.size(); j++) {
            uint64_t m = elem ^ (1 << decPoints[j]);
            correctionTableFW[i][decPoints.size() - j - 1] = rankLinear(m);
        }
    }
}

string All::toBinaryString(uint64_t i) {
    uint64_t num = i;
    string bin;
    while (num > 0) {
        bin = std::to_string(num % 2) + bin;
        num /= 2;
    }
    while (bin.size() < maxbits) {
        bin = "0" + bin;
    }
    return bin;
}

string All::toBinaryStringNBB(uint64_t i) {
    uint64_t num = i;
    string bin;
    while (num > 0) {
        bin = std::to_string(num % 2) + bin;
        num /= 2;
    }
    while (bin.size() < decPoints.size()) {
        bin = "0" + bin;
    }
    return bin;
}

void All::print() {
    cout << "Input:\t{";
    for (int i = 0; i < size; i++) cout << input[i] << ", ";
    cout << "}" << endl;
    cout << "Compressed Keys:\t{";
    for (int i = 0; i < size; i++) cout << extractedinput[i] << ", ";
    cout << "}" << endl;
    cout << "Decesion Points:\t{";
    for (auto i = decPoints.begin(); i != decPoints.end(); ++i)
        cout << *i << ", ";
    cout << "}\tMaxbits: " << maxbits << endl;
    cout << "AnswerTable:\t{";
    for (int i = 0; i < ansSize; i++) cout << answerTable[i] << ", ";
    cout << "}" << endl;
    cout << "DecPointRank:\t{";
    for (int i = 0; i < maxbits; i++) cout << decPointRank[i] << ", ";
    cout << "}" << endl;
    cout << "Mask: " << mask << "\tNBB: " << decPoints.size()
         << "\ncorrection Table: \n";
    for (int i = 0; i < size; i++) {
        for (int j = 0; j <= maxbits; j++) {
            cout << correctionTable[i][j] << ", ";
        }
        cout << endl;
    }
}

int All::getNBBCount() { return decPoints.size(); }

std::random_device rd;
std::mt19937_64 eng(rd());
std::uniform_int_distribution<uint64_t> distr;
uint64_t *generateInput(int size) {
    uint64_t *arr = new uint64_t[size];

    for (int i = 0; i < size;) {
        uint64_t k = distr(eng) % (uint64_t)pow(2, 31);
        if (!std::binary_search(arr, arr + size, k) && k != 0) arr[i++] = k;
    }
    std::sort(arr, arr + size);
    return arr;
}

void show(uint64_t *input, unsigned int size) {
    cout << "Input: {";
    for (int i = 0; i < size; i++) {
        cout << input[i] << ", ";
    }
    cout << "} " << endl;
}

// Function to get memory usage from /proc/self/stat
long getMemoryUsage() {
    std::ifstream stat_file("/proc/self/stat");
    std::string stat_line;
    if (stat_file.is_open()) {
        std::getline(stat_file, stat_line);
        stat_file.close();
    }

    std::istringstream iss(stat_line);
    std::vector<std::string> stats((std::istream_iterator<std::string>(iss)),
                                   std::istream_iterator<std::string>());
    if (stats.size() > 22) {
        return std::stol(stats[22]);  // RSS (Resident Set Size) in pages
    }
    return 0;
}

int main(int argc, char *argv[]) {
    const int size = atoi(argv[1]);
    uint64_t *input = new uint64_t[size];
    input = generateInput(size);

    uint64_t max = input[size - 1];

    All pat_tree(input, size);
    int nbb = pat_tree.getNBBCount();

    // can't fit into 256 bits of size*nbb > 256bits
    if ((size * nbb) <= 256) {
        uint64_t n = (int)pow(2, 25);
        uint64_t *A = new uint64_t[n];
        for (int i = 0; i < n; i++) {
            A[i] = distr(eng) % max;
        }

        auto sumAFK = 0, sumFW = 0, sumFWM = 0,sumFWMI = 0, sumLinear = 0, sumBSI = 0;
        auto timeAFK = 0, timeFW = 0, timeFWM = 0,timeFWMI = 0, timeLinear = 0, timeBSI = 0;
        auto spaceAFK = 0, spaceFW = 0, spaceFWM = 0, spaceLinear = 0, spaceBSI = 0;
        auto memory_before = 0, memory_after = 0;

        auto start_time = std::chrono::high_resolution_clock::now();
        //memory_before = getMemoryUsage();
        for (int i = 0; i < n; i++) {
            sumAFK += pat_tree.rankAFK(A[i]);
        }
        //memory_after = getMemoryUsage();
        timeAFK = std::chrono::duration_cast<std::chrono::microseconds>(
                      std::chrono::high_resolution_clock::now() - start_time)
                      .count();
        // spaceAFK = memory_after - memory_before;

        start_time = std::chrono::high_resolution_clock::now();
        //memory_before = getMemoryUsage();
        for (int i = 0; i < n; i++) {
            sumFWM += pat_tree.rankFWM(A[i]);
        }
        //memory_after = getMemoryUsage();
        timeFWM = std::chrono::duration_cast<std::chrono::microseconds>(
                      std::chrono::high_resolution_clock::now() - start_time)
                      .count();
        // spaceFWM = memory_after - memory_before;

        start_time = std::chrono::high_resolution_clock::now();
        //memory_before = getMemoryUsage();
        for (int i = 0; i < n; i++) {
            sumFWMI += pat_tree.rankSIMDXOR<8>(A[i]);
        }
        //memory_after = getMemoryUsage();
        timeFWMI = std::chrono::duration_cast<std::chrono::microseconds>(
                      std::chrono::high_resolution_clock::now() - start_time)
                      .count();
        // spaceFWMI = memory_after - memory_before;

        start_time = std::chrono::high_resolution_clock::now();
        //memory_before = getMemoryUsage();
        for (int i = 0; i < n; i++) {
            sumLinear += pat_tree.rankLinear(A[i]);
        }
        //memory_after = getMemoryUsage();
        timeLinear = std::chrono::duration_cast<std::chrono::microseconds>(
                         std::chrono::high_resolution_clock::now() - start_time)
                         .count();
        // spaceLinear = memory_after - memory_before;

        switch (size) {
            case 4:
                start_time = std::chrono::high_resolution_clock::now();
                //memory_before = getMemoryUsage();
                for (int i = 0; i < n; i++) {
                    sumBSI += pat_tree.rankBSI<4>(A[i]);
                }
                //memory_after = getMemoryUsage();
                timeBSI = std::chrono::duration_cast<std::chrono::microseconds>(
                                std::chrono::high_resolution_clock::now() - start_time)
                                .count();
                spaceBSI = memory_after - memory_before;
                break;
            case 8:
                start_time = std::chrono::high_resolution_clock::now();
                //memory_before = getMemoryUsage();
                for (int i = 0; i < n; i++) {
                    sumBSI += pat_tree.rankBSI<8>(A[i]);
                }
                //memory_after = getMemoryUsage();
                timeBSI = std::chrono::duration_cast<std::chrono::microseconds>(
                                std::chrono::high_resolution_clock::now() - start_time)
                                .count();
                spaceBSI = memory_after - memory_before;
                break;
            case 16:
                start_time = std::chrono::high_resolution_clock::now();
                //memory_before = getMemoryUsage();
                for (int i = 0; i < n; i++) {
                    sumBSI += pat_tree.rankBSI<16>(A[i]);
                }
                //memory_after = getMemoryUsage();
                timeBSI = std::chrono::duration_cast<std::chrono::microseconds>(
                                std::chrono::high_resolution_clock::now() - start_time)
                                .count();
                spaceBSI = memory_after - memory_before;
                break;
            case 32:
                start_time = std::chrono::high_resolution_clock::now();
                //memory_before = getMemoryUsage();
                for (int i = 0; i < n; i++) {
                    sumBSI += pat_tree.rankBSI<32>(A[i]);
                }
                //memory_after = getMemoryUsage();
                timeBSI = std::chrono::duration_cast<std::chrono::microseconds>(
                                std::chrono::high_resolution_clock::now() - start_time)
                                .count();
                spaceBSI = memory_after - memory_before;
                break;
            case 64:
                start_time = std::chrono::high_resolution_clock::now();
                //memory_before = getMemoryUsage();
                for (int i = 0; i < n; i++) {
                    sumBSI += pat_tree.rankBSI<64>(A[i]);
                }
                //memory_after = getMemoryUsage();
                timeBSI = std::chrono::duration_cast<std::chrono::microseconds>(
                                std::chrono::high_resolution_clock::now() - start_time)
                                .count();
                spaceBSI = memory_after - memory_before;
                break;
            case 128:
                start_time = std::chrono::high_resolution_clock::now();
                //memory_before = getMemoryUsage();
                for (int i = 0; i < n; i++) {
                    sumBSI += pat_tree.rankBSI<128>(A[i]);
                }
                //memory_after = getMemoryUsage();
                timeBSI = std::chrono::duration_cast<std::chrono::microseconds>(
                                std::chrono::high_resolution_clock::now() - start_time)
                                .count();
                spaceBSI = memory_after - memory_before;
                break;
            case 256:
                start_time = std::chrono::high_resolution_clock::now();
                //memory_before = getMemoryUsage();
                for (int i = 0; i < n; i++) {
                    sumBSI += pat_tree.rankBSI<256>(A[i]);
                }
                //memory_after = getMemoryUsage();
                timeBSI = std::chrono::duration_cast<std::chrono::microseconds>(
                                std::chrono::high_resolution_clock::now() - start_time)
                                .count();
                spaceBSI = memory_after - memory_before;
                break;
            case 512:
                start_time = std::chrono::high_resolution_clock::now();
                //memory_before = getMemoryUsage();
                for (int i = 0; i < n; i++) {
                    sumBSI += pat_tree.rankBSI<512>(A[i]);
                }
                //memory_after = getMemoryUsage();
                timeBSI = std::chrono::duration_cast<std::chrono::microseconds>(
                                std::chrono::high_resolution_clock::now() - start_time)
                                .count();
                spaceBSI = memory_after - memory_before;
                break;
            default:
                cout << "Unsupported size for BSI rank." << endl;
                break;
        }

        cout << size << "\t" << nbb << "\t" << timeLinear  << "\t" << timeAFK
             << "\t" << timeFWM << "\t" << timeFWMI << "\t" << timeBSI<< "\t["
             << (sumLinear+sumAFK-sumLinear+sumFWM-sumLinear+sumFWMI/
                  sumLinear/sumBSI)
             << "]" << endl;

        delete[] A;
        delete[] input;
    }

    return 0;
}