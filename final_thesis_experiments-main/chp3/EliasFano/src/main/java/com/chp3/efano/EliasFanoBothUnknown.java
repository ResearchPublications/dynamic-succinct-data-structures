/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chp3.efano;


/**
 *
 * @author ai146
 * For each block b_i, the universe is u_i = 2^i - 1 and n_i = 2^{i-1}
 */
public class EliasFanoBothUnknown {

    EliasFanoUniverseKnown[] cluster; //The Elias Fano Cluster
    long[] prefixSum;//stores the number of elements in each block as prefixSum
    long[] minVal;//stores the minimum value of each block

    long u;

    public EliasFanoBothUnknown() {
        this.cluster = new EliasFanoUniverseKnown[64];
        for (int i = 1; i < cluster.length; i++) {
            u = (long) Math.pow(2, i);
            this.cluster[i] = new EliasFanoUniverseKnown(u);
        }
        this.prefixSum = new long[64];
        this.minVal = new long[64];
    }

    public final void append(long i) {
        int block = bitLength(i) + 1;
        cluster[block].append(i);
        updatePrefixSum(block);
    }

    public final void updatePrefixSum(int block) {
        prefixSum[block]++;
        for (int i = block + 1; i < 64; i++) {
            prefixSum[i] = prefixSum[i - 1] + cluster[i].size();
        }
    }

    public static int bitLength(long i) {
        return Long.SIZE - Long.numberOfLeadingZeros(i);
    }

    public final int findBlock(long i) {
        for (int j = 0; j < prefixSum.length; j++) {
            if (prefixSum[j] >= i) {
                return j;
            }
        }
        return -1;
    }

    public final long access(long i) {
        int block = findBlock(i);
        return cluster[block].access((i - prefixSum[block - 1]));
    }

    public String print() {
        String s = "{";
        for (int i = 1; i < cluster.length; i++) {
            for (int j = 0; j < cluster[i].size(); j++) {
                s += cluster[i].access(j) + ", ";
            }
        }
        return s + "}";
    }

    public static void main(String[] args) {
        int[] array = new int[]{3, 6, 7, 9, 13, 24, 34, 65, 889, 1234, 3465, 6595, 46656, 65684, 98457, 846436, 964767, 6685853, 7896858, 8985378};

        EliasFanoBothUnknown cl = new EliasFanoBothUnknown();
        for (int i : array) {
            cl.append(i);
        }
        System.out.println(cl.print());

    }
}
