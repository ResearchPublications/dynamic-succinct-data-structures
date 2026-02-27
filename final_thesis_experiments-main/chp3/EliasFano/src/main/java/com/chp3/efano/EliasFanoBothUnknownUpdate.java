/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chp3.efano;


/**
 *
 * @author ai146 For each block b_i, the universe is u_i = 2^i - 1 and n_i =
 * 2^{i-1}
 */
public class EliasFanoBothUnknownUpdate {

    EliasFanoUniverseKnown[] cluster; //The Elias Fano Cluster
    long[] prefixSum;//stores the number of elements in each block as prefixSum
    long[] lowerBounds;//stores the minimum value of each block
    long count;//total number of elements in the sequence

    public EliasFanoBothUnknownUpdate() {
        this.cluster = new EliasFanoUniverseKnown[64];
        this.prefixSum = new long[64];
        this.lowerBounds = new long[64];

        for (int i = 1; i < cluster.length; i++) {
            long u = (long) Math.pow(2, i);
            this.lowerBounds[i] = (long) Math.pow(2, i - 1);
            this.cluster[i] = new EliasFanoUniverseKnown(u);
        }
    }

    public final void append(long i) {
        int block = bitLength(i);
        cluster[block].append(i - lowerBounds[block]);
        updatePrefixSum(block);
        count++;
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
        return cluster[block].access((i - prefixSum[block - 1]) - 1) + lowerBounds[block];
    }

    public String print() {
        String s = "{";
        for (int i = 1; i <= count; i++) {
            s += access(i) + ", ";
        }
        return s + "}";
    }
    
    public void spaceUsage(){
        for (int i = 1; i < cluster.length; i++) {
            cluster[i].spaceUsage();
        }
    }
    
    public long[] getPrefixSum(){
        return prefixSum;
    }

    public static void main(String[] args) {
        int[] array = new int[]{3, 6, 7, 9, 13, 24, 34, 65, 889, 1234, 3465, 6595, 46656, 65684, 98457, 846436, 964767, 6685853, 7896858, 8985378, 764575454};

        EliasFanoBothUnknownUpdate cl = new EliasFanoBothUnknownUpdate();
        for (int i : array) {
            cl.append(i);
        }
        System.out.println(cl.print());
        cl.spaceUsage();
    }
}
