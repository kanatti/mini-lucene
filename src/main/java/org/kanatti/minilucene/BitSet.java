package org.kanatti.minilucene;

public interface BitSet {
    void set(int i);
    boolean get(int i);
    int nextSetBit(int from);
    int length();
}
