package org.kanatti.minilucene.search;

/**
 * Collects results (which are just docIds) from a search phase.
 * Collector is a like a sink that collects docIds and builds something out of it,
 * like hit count, top docs, aggregations, sorted docs etc.
 */
public interface Collector {
    LeafCollector getLeafCollector();
}
