package org.kanatti.minilucene.search;

/**
 * Counts how many documents matched.
 */
public class TotalHitCountCollector implements Collector {
    // Shared across leafs and not thread-safe.
    // So a single collector is always used within a thread.
    // Seems CollectorManager lets you parallelize, something to check.
    private int totalHits;

    public int getTotalHits() {
        return totalHits;
    }

    @Override
    public LeafCollector getLeafCollector() {
        return new LeafCollector() {
            @Override
            public void collect(int docId) {
                totalHits++;
            }
        };
    }
}
