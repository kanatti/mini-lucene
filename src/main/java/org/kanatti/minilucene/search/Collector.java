package org.kanatti.minilucene.search;

/**
 * Collects results (which are just docIds) from a search phase.
 * Collector is a like a sink that collects docIds and builds something out of it,
 * like hit count, top docs, aggregations, sorted docs etc.
 */
public interface Collector {
    /**
     * Collection is done per leaf, but state can be at a parent collector level.
     * So each leafcollector can collect into the parent state.
     * See {@link TotalHitCountCollector} for example
     */
    LeafCollector getLeafCollector();
}
