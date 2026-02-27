/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chp3.efano;
import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.sux4j.bits.SimpleSelect;

/**
 *
 * @author ai146
 */
public final class ResizableBitVector {

    protected LongArrayBitVector bitvector;
    protected long length;
    protected long capacity;
    static final int INIT_CAPACITY = 128;

    /**
     * Constructor for unknown initial capacity. Default initial capacity is
     * fixed to 2.
     */
    @SuppressWarnings("unchecked")
    public ResizableBitVector() {
        bitvector = LongArrayBitVector.getInstance().length(INIT_CAPACITY);
        capacity = bitvector.length();
        length = 0;
    }

    /**
     * Constructor for known initial capacity and maximum allowed capacity.
     *
     */
    public ResizableBitVector(final long capacity) {
        bitvector = LongArrayBitVector.getInstance().length(capacity);
        this.capacity = capacity;
        length = 0;
    }
    
    public void append(int i){
        resize();
        bitvector.append(i, 1);
        length++;
    }
    
    public void resize(){
        if(length == capacity){
            long newlength = (long) (length + Math.ceil(length >> 6));
            bitvector.length(newlength);
            capacity = newlength;
        }
        
    }
    
    public String print(){
        String s = "{";
        for(long l : bitvector.bits()){
            s+= Long.toBinaryString(l);
        }
        return s+= "}";
    }
    
       
    public BitVector bitVector(){
        return bitvector;
    }
    
    public void set(long index){
        bitvector.set(index);
    }
    
    
    public SimpleSelect buildSelectIndex(){
        return new SimpleSelect(bitVector());
    }
    
    public static void main(String[] args) {
        ResizableBitVector bv = new ResizableBitVector();
        for(int i = 0; i < 10000; i++){
            bv.append(i&1);
        }
        System.out.println(bv.print());
    }
}
