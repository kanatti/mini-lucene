package org.kanatti.minilucene.search;


/**
 * Converts a {@link BitSet} into a {@link DocIdSetIterator}.
 */
public class BitSetDISI implements DocIdSetIterator {
    private final BitSet bitset;
    private int doc = NOT_STARTED;

    public BitSetDISI(BitSet bitset) {
        this.bitset = bitset;
    }

    @Override
    public int docId() {
        if (doc == NOT_STARTED)
            return NOT_STARTED;
        if (doc >= bitset.length())
            return NO_MORE_DOCS;
        return doc;
    }

    @Override
    public int nextDoc() {
        return advance(doc + 1);
    }

    @Override
    public int advance(int target) {
        if (target >= bitset.length())
            return NO_MORE_DOCS;
        int next = bitset.nextSetBit(target);
        if (next == -1)
            return NO_MORE_DOCS;
        doc = next;
        return doc;
    }

}
