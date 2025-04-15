package org.kanatti.minilucene.search;

/**
 * Collects results at a segment level. No scoring yet.
 */
public interface LeafCollector {
    void collect(int docId);
}
