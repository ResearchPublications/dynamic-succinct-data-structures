/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chp3.efano;

import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.fastutil.longs.LongBigListIterator;
import java.text.DecimalFormat;
import java.util.Random;

/**
 *
 * @author ai146
 */
public final class ResizableBitArray {

    protected LongBigList bitarray;
    protected long length;
    protected long capacity;
    protected int width;
    protected long numbits;
    protected double factor;
    static final int INIT_CAPACITY = 64;

    /**
     * Constructor for unknown initial capacity. Default initial capacity is
     * fixed to 2.
     *
     * @param width
     */
    public ResizableBitArray(final int width) {
        this(width, (long) Math.ceil(128 / width), 6);
    }

    /**
     * Constructor for unknown initial capacity. Default initial capacity is
     * fixed to 2.
     *
     * @param width
     * @param resizefactor
     */
    public ResizableBitArray(final int width, final int resizefactor) {
        this(width, (long) Math.ceil(128 / width), resizefactor);
    }

    /**
     * Constructor for known initial capacity and maximum allowed capacity.
     *
     * @param width
     * @param capacity
     * @param resizefactor
     */
    public ResizableBitArray(final int width, final long capacity, final double resizefactor) {
        this.bitarray = LongArrayBitVector.getInstance().asLongBigList(width);
        this.bitarray.size(capacity);
        this.factor = resizefactor;
        this.width = width;
        this.numbits = 0;
        this.length = 0;
        this.capacity = capacity;
    }

    public void append(long i) {
        resize();
        bitarray.add(i);
        numbits += width;
        length++;
    }

    public void resize() {
        if (length == capacity) {
            long extra = (long) Math.ceil((numbits * width) / Math.pow(2, factor));
            long newlength = numbits + extra;
            bitarray.size(newlength);
            capacity = newlength / width;
            //System.err.println("(" + width + "," + factor + ") \t (" + numbits + "," + newlength + ") \t (" + length + ", " + capacity + ")");
        }
    }

    public String print() {
        LongBigListIterator iterator = bitarray.iterator();
        String s = "{";
        while (iterator.hasNext()) {
            s += iterator.next() + ", ";
        }
        return s += "}";
    }

    public LongBigList bigList() {
        return bitarray;
    }

    public void add(long element) {
        bitarray.add(element);
    }

    public String spaceUsed() {
        DecimalFormat df = new DecimalFormat("#.###");
        double normal = length * width;
        double used = capacity * width;
        double diff = used - normal;
        return df.format(100 * (diff / normal)) + "%";
    }

    public static void main(String[] args) {
        Random r = new Random();
        DecimalFormat df = new DecimalFormat("#.###");
        int width = 1;
        int factor = 1;
        int length = (int) Math.pow(2, 16) ;
        double time = 0;
        String space = null;
        ResizableBitArray bv = null;

        System.out.println("width \t factor \t time \t space");
        for (width = 64; width >= 1; width /= 2) {
            long mask = width == 64 ? -1L : (1L << width) - 1;
            long[] values = new long[length];
            for (int i = 0; i < length; i++) {
                values[i] = r.nextLong() & mask;
            }
            for (factor = 0; factor <= 6; factor++) {
                System.out.print(width + "\t " + factor + "\t ");
                //for (int j = 0; j < 5; j++) {
                bv = new ResizableBitArray(width, factor);
                time = System.nanoTime();
                for (int i = 0; i < length; i++) {
                    bv.append(values[i]);
                }
                time = (System.nanoTime() - time) / 1E9;
                System.out.println(time+ "\t"+bv.spaceUsed());
            }
            //System.err.println("************************************");
        }

    }
}
