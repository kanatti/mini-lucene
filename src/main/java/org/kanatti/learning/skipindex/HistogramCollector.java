package org.kanatti.learning.skipindex;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Collector that builds a histogram by reading actual values.
 * This forces reading DocValues even when skip index says YES.
 */
public class HistogramCollector implements Collector {

    private final String field;
    private final Map<Long, Integer> histogram = new HashMap<>();

    public HistogramCollector(String field) {
        this.field = field;
    }

    @Override
    public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
        NumericDocValues docValues = context.reader().getNumericDocValues(field);

        return new LeafCollector() {
            @Override
            public void setScorer(Scorable scorer) {}

            @Override
            public void collect(int doc) throws IOException {
                if (docValues.advanceExact(doc)) {
                    long value = docValues.longValue();
                    histogram.merge(value, 1, Integer::sum);
                }
            }
        };
    }

    @Override
    public ScoreMode scoreMode() {
        return ScoreMode.COMPLETE_NO_SCORES;
    }

    public Map<Long, Integer> getHistogram() {
        return histogram;
    }

    public int getTotalHits() {
        return histogram.values().stream().mapToInt(Integer::intValue).sum();
    }
}
