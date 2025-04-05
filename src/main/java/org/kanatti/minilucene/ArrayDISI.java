package org.kanatti.minilucene;

public class ArrayDISI implements DocIdSetIterator {
    private final int[] docIds;
    private int idx = NOT_STARTED;

    public ArrayDISI(int[] docIds) {
        this.docIds = docIds;
    }

    @Override
    public int docId() {
        if (idx == NOT_STARTED)
            return NOT_STARTED;
        if (idx >= docIds.length)
            return NO_MORE_DOCS;
        return docIds[idx];
    }

    @Override
    public int nextDoc() {
        idx = idx + 1;
        if (idx >= docIds.length)
            return NO_MORE_DOCS;
        return docIds[idx];
    }

    @Override
    public int advance(int target) {
        if (idx >= docIds.length)
            return NO_MORE_DOCS;

        int doc;
        do {
            doc = nextDoc();
        } while (doc < target);
        return doc;
    }

}
