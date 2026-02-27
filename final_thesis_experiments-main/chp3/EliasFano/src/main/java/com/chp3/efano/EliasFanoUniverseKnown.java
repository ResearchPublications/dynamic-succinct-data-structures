/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chp3.efano;

/**
 *
 * @author ai146
 */
import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.sux4j.bits.SimpleSelect;

public final class EliasFanoUniverseKnown {

    protected int N = 2;
    protected long U;
    protected int size = 0;
    protected long last_inserted = -1;
    protected SimpleSelect selector = null;
    protected static long lowerBitsMask;
    protected LongBigList lowerBitsList;
    protected BitVector upperBits;
    protected long l;
    protected LongArrayBitVector upperbitsnew;
    protected LongBigList lowerBitsListnew;

    public EliasFanoUniverseKnown(final long u) {
        this.U = u;
        l = Math.max(0, Fast.mostSignificantBit(U / N));
        lowerBitsMask = (1L << l) - 1;
        lowerBitsList = LongArrayBitVector.getInstance().asLongBigList((int) l);
        lowerBitsList.size(N);
        upperBits = LongArrayBitVector.getInstance().length(N + (U >>> l) + 1);
    }

    protected void append(final long element) {
        resize();
        lowerBitsList.set(size, element & lowerBitsMask);
        upperBits.set((element >>> l) + size);
        last_inserted = element;
        size++;
    }

    public int size() {
        return size;
    }

    protected void resize() {
        if (size == N) {
            //N *= 2;
            N = (int)(N + Math.ceil(N/64.0));
            l = Math.max(0, Fast.mostSignificantBit(U / N));
            lowerBitsMask = (1L << l) - 1;

            lowerBitsListnew = LongArrayBitVector.getInstance().asLongBigList((int) l);
            lowerBitsListnew.size(N);
            upperbitsnew = LongArrayBitVector.getInstance().length(N + (U >>> l) + 1);

            for (int i = 0; i < size; i++) {
                long element = access(i);
                lowerBitsListnew.set(i, element & lowerBitsMask);
                upperbitsnew.set((element >>> l) + i);
            }
            lowerBitsList = lowerBitsListnew;
            upperBits = upperbitsnew;
            selector = null;
        }

    }

    public long access(final long i) {
        buildSelectIndex();
        long up = selector.select(i) - (i);
        long low = lowerBitsList.get(i);
        return (int) ((up << l) | low);
    }

    public void buildSelectIndex() {
        if (selector == null) {
            selector = new SimpleSelect(upperBits);
        }
    }

    public void print() {
        String s = "{";
        for (int i = 0; i < size; i++) {
            s += (access(i) + ", ");
        }
        System.out.print(s + "}\n\n");
    }
    
    public double spaceUsage(){
        long itlb = (2*size) + (size*l);
        long used = (upperBits.bits().length * 64) + (lowerBitsList.size()*l) ;
        double diff = ((double)(used-itlb)/itlb);
        //System.err.println(itlb+ "\t "+ used+"\t"+diff+"%");
        return diff;
    }

    public static void main(String[] args) {
        int[] array = new int[]{3, 6, 7, 9, 13, 24, 34, 65, 889, 1234, 6574, 46656, 98457846, 436564767, 868585378};

        EliasFanoUniverseKnown cl = new EliasFanoUniverseKnown(array[array.length - 1]);
        for (int i : array) {
            cl.append(i);
        }
        cl.print();
        cl.spaceUsage();

    }

}
