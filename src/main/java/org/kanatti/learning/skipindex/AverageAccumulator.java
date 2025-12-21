package org.kanatti.learning.skipindex;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;

import java.io.IOException;

/**
 * Collector that computes average of a numeric field.
 * Must read all matched doc values - similar to HistogramCollector.
 */
public class AverageAccumulator implements Collector {

    private final String field;
    private long sum = 0;
    private int count = 0;

    public AverageAccumulator(String field) {
        this.field = field;
    }

    @Override
    public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
        SortedNumericDocValues docValues = context.reader().getSortedNumericDocValues(field);

        return new LeafCollector() {
            @Override
            public void setScorer(Scorable scorer) {}

            @Override
            public void collect(int doc) throws IOException {
                if (docValues != null && docValues.advanceExact(doc)) {
                    // For single-valued field, just get first value
                    sum += docValues.nextValue();
                    count++;
                }
            }
        };
    }

    @Override
    public ScoreMode scoreMode() {
        return ScoreMode.COMPLETE_NO_SCORES;
    }

    public double getAverage() {
        return count > 0 ? sum / (double) count : 0.0;
    }

    public int getCount() {
        return count;
    }
}
