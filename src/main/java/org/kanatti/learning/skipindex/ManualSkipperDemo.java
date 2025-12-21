package org.kanatti.learning.skipindex;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValuesSkipper;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Demonstrates manual use of DocValuesSkipper to iterate through values
 * and make skip decisions.
 */
public class ManualSkipperDemo {

    public static void main(String[] args) throws IOException {
        Path indexDir = Paths.get("data/skipindex/with-skip-index");

        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexDir))) {
            LeafReader leafReader = reader.leaves().get(0).reader();

            System.out.println("=== Manual Skip Index Usage Demo ===\n");

            // Query: year >= 2024
            long queryMin = 2024;
            long queryMax = Long.MAX_VALUE;

            System.out.println("Query: year >= " + queryMin + "\n");

            // Method 1: Using skipper
            int matchesWithSkipper = iterateWithSkipper(leafReader, queryMin, queryMax);

            // Method 2: Without skipper (naive)
            int matchesWithoutSkipper = iterateWithoutSkipper(leafReader, queryMin, queryMax);

            System.out.println("\n=== Results ===");
            System.out.println("With skipper:    " + matchesWithSkipper + " matches");
            System.out.println("Without skipper: " + matchesWithoutSkipper + " matches");
        }
    }

    private static int iterateWithSkipper(LeafReader reader, long queryMin, long queryMax)
            throws IOException {

        System.out.println("--- Using Skipper ---");

        DocValuesSkipper skipper = reader.getDocValuesSkipper("year");
        if (skipper == null) {
            System.out.println("No skip index available!");
            return 0;
        }

        NumericDocValues docValues = reader.getNumericDocValues("year");

        System.out.println("Skip index has " + skipper.numLevels() + " levels");

        int matches = 0;
        int docsChecked = 0;
        int intervalsSkipped = 0;
        int intervalsAccepted = 0;
        int intervalsMaybe = 0;

        int docID = 0;
        int maxDoc = reader.maxDoc();

        while (docID < maxDoc) {
            // Advance skipper to current position
            skipper.advance(docID);

            // Check level 0 (finest granularity)
            int level = 0;
            int minDocID = skipper.minDocID(level);
            int maxDocID = skipper.maxDocID(level);
            long minValue = skipper.minValue(level);
            long maxValue = skipper.maxValue(level);
            int docCount = skipper.docCount(level);

            // Make skip decision
            if (maxValue < queryMin) {
                // NO: Skip entire interval
                intervalsSkipped++;
                System.out.println("Interval [" + minDocID + "-" + maxDocID + "]: " +
                                 "[" + minValue + "-" + maxValue + "] -> SKIP");
                docID = maxDocID + 1;
            } else if (minValue >= queryMin && maxValue <= queryMax) {
                // YES: All docs match
                intervalsAccepted++;
                int intervalMatches = docCount;
                matches += intervalMatches;
                System.out.println("Interval [" + minDocID + "-" + maxDocID + "]: " +
                                 "[" + minValue + "-" + maxValue + "] -> ACCEPT ALL (" +
                                 intervalMatches + " docs)");
                docID = maxDocID + 1;
            } else {
                // MAYBE: Check individual docs
                intervalsMaybe++;
                System.out.println("Interval [" + minDocID + "-" + maxDocID + "]: " +
                                 "[" + minValue + "-" + maxValue + "] -> CHECK INDIVIDUALLY");

                // Check each doc in this interval
                for (int d = minDocID; d <= maxDocID && d < maxDoc; d++) {
                    docsChecked++;
                    if (docValues.advanceExact(d)) {
                        long value = docValues.longValue();
                        if (value >= queryMin && value <= queryMax) {
                            matches++;
                        }
                    }
                }
                docID = maxDocID + 1;
            }
        }

        System.out.println("\nStats:");
        System.out.println("  Intervals skipped: " + intervalsSkipped);
        System.out.println("  Intervals accepted: " + intervalsAccepted);
        System.out.println("  Intervals checked individually: " + intervalsMaybe);
        System.out.println("  Individual docs checked: " + docsChecked);

        return matches;
    }

    private static int iterateWithoutSkipper(LeafReader reader, long queryMin, long queryMax)
            throws IOException {

        System.out.println("\n--- Without Skipper (naive) ---");

        NumericDocValues docValues = reader.getNumericDocValues("year");

        int matches = 0;
        int docsChecked = 0;
        int maxDoc = reader.maxDoc();

        for (int docID = 0; docID < maxDoc; docID++) {
            docsChecked++;
            if (docValues.advanceExact(docID)) {
                long value = docValues.longValue();
                if (value >= queryMin && value <= queryMax) {
                    matches++;
                }
            }
        }

        System.out.println("Stats:");
        System.out.println("  Docs checked: " + docsChecked);

        return matches;
    }
}
