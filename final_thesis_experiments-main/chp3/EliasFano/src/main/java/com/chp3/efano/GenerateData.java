/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chp3.efano;

import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author ai146
 */
public class GenerateData {
    static Random r = new Random(1);
    
    public static long[] generateIncreasingRandomLongs(int size){
        long[] res = new long[size];
        res[0] = 1;
        for (int i = 1; i < size; i++) {
            res[i] = res[i-1] + (r.nextLong()& 0x7FFFFFFFFFFFFFFFL)%7;
        }
        Arrays.sort(res);
        return res;
    }
    
    public static int[] generateRandomIndexes(long max, int size){
        int[] res = new int[size];
        for (int i = 1; i < size; i++) {
            res[i] = (int) ((r.nextLong() & 0x7FFFFFFFFFFFFFFFL) % max);
        }
        return res;
    }
}
