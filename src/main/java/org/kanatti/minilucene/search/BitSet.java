package org.kanatti.minilucene.search;

public interface BitSet {
    void set(int i);
    boolean get(int i);
    int nextSetBit(int from);
    int length();
}
