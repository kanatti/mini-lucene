package org.kanatti.minilucene.search;

public class ArrayBitSet implements BitSet {
    private final boolean[] bits;

    public ArrayBitSet(int size) {
        bits = new boolean[size];
    }

    @Override
    public void set(int i) {
        if (i < 0 || i >= bits.length)
            throw new IllegalArgumentException("Out of range");
        bits[i] = true;
    }

    @Override
    public boolean get(int i) {
        if (i < 0 || i >= bits.length)
            throw new IllegalArgumentException("Out of range");
        return bits[i];
    }

    @Override
    public int nextSetBit(int from) {
        if (from < 0 || from >= bits.length)
            throw new IllegalArgumentException("Out of range");

        for (int i = from; i < bits.length; i++) {
            if (bits[i])
                return i;
        }

        return -1;
    }

    @Override
    public int length() {
        return bits.length;
    }

}
