/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chp3.efano;

import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.sux4j.bits.SimpleSelect;

/**
 *
 * @author ai146
 */
public class ClassicEliasFano {

    private int size;
    private int capacity;
    int n, l;
    long u;
    private LongArrayBitVector upperbits;
    private LongBigList lowerbits;
    int lowerBitsMask;
    private SimpleSelect selector = null;
    LongBigList lowerBitsList;
    BitVector upperBits;
    int pos = 0;

    public ClassicEliasFano(int u, int n) {
        size = 0;
        this.n = this.capacity = n;
        this.u = u;
        l = Math.max(0, Fast.mostSignificantBit(u / n));
        lowerBitsMask = (1 << l) - 1;
        lowerBitsList = LongArrayBitVector.getInstance().asLongBigList((int) l);
        lowerBitsList.size(n);
        System.err.println("UP:"+(n + (u >>> l) + 1)+"\tLOW:"+n+"\tl:"+l+"\tN:"+n);
        upperBits = LongArrayBitVector.getInstance().length(n + (u >>> l) + 1);
    }

    public ClassicEliasFano(int u) {
        this(u, 2);
    }

    public ClassicEliasFano(int[] array) {
        this(array[array.length - 1], array.length);
        compress(array);
    }

    public final void compress(int[] array) {
        for (int i : array) {
            append(i);
        }
        buildSelectIndex();
    }

    public void buildSelectIndex() {
        selector = new SimpleSelect(upperBits);
    }

    public void append(int element) {
        lowerBitsList.set(pos, element & lowerBitsMask);
        upperBits.set((element >>> l) + pos);
        size++;
        pos++;
    }

    public final int access(final long i) {
        long up = selector.select(i) - (i);
        long low = lowerBitsList.get(i);
        return (int) ((up << l) | low);
    }

    public int capacity() {
        return capacity;
    }

    public int size() {
        return size;
    }
    
    public void print(){
        for(long l : upperBits.bits()){
            System.err.print(Long.toBinaryString(l)+"");
        }
        String s = "{";
        for(int i = 0; i < size; i++)
            s+=(access(i)+", ");
        System.out.print(s+"}\n\n");
    }

//    public static void main(String[] args) {
//        int[] array = new int[]{3, 6, 7, 9, 13, 24, 34, 65, 889, 1234, 657456, 98457846, 436564767, 868585378};
//
//        ClassicEliasFano cl = new ClassicEliasFano(array);
//        cl.print();
//
//    }

}
