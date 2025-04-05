package org.kanatti.minilucene;

/**
 * A way to iterate through doc-ids.
 */
public interface DocIdSetIterator {
    /**
     *  A sentinel to indicate that we have exhausted all the docs in iterator.
     */
    int NO_MORE_DOCS = Integer.MAX_VALUE;

    /**
     * A sentinel to indicate that we havent started iteration.
     */
    int NOT_STARTED = -1;

    /**
     * Returns current docId.
     */
    int docId();

    /**
     * Advances by one step.
     */
    int nextDoc();

    /**
     * Advances to first doc with docId >= target
     */
    int advance(int target);
}
