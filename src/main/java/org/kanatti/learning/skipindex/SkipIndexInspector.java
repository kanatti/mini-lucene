package org.kanatti.learning.skipindex;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValuesSkipper;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.store.FSDirectory;
import org.kanatti.common.TablePrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Inspects skip index structure and shows interval details.
 */
public class SkipIndexInspector {

    public static void main(String[] args) throws IOException {
        Path indexDir = Paths.get("data/skipindex/with-skip-index");

        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexDir))) {
            LeafReader leafReader = reader.leaves().get(0).reader();

            System.out.println("=== Skip Index Inspector ===\n");

            DocValuesSkipper skipper = leafReader.getDocValuesSkipper("year");
            if (skipper == null) {
                System.out.println("No skip index available!");
                return;
            }

            System.out.println("Field: year");
            System.out.println("Total docs: " + leafReader.maxDoc());
            System.out.println("Skip index levels: " + skipper.numLevels());
            System.out.println();

            // Inspect level 0 (finest granularity)
            inspectLevel(skipper, 0, leafReader.maxDoc());
        }
    }

    private static void inspectLevel(DocValuesSkipper skipper, int level, int maxDoc)
            throws IOException {

        System.out.println("Level " + level + " intervals:");

        TablePrinter table = new TablePrinter(
            "Interval #",
            "Doc Range",
            "Size",
            "Value Range",
            "Doc Count",
            "Density"
        );

        int intervalNum = 0;
        int docID = 0;

        while (docID < maxDoc) {
            skipper.advance(docID);

            int minDocID = skipper.minDocID(level);
            int maxDocID = skipper.maxDocID(level);
            long minValue = skipper.minValue(level);
            long maxValue = skipper.maxValue(level);
            int docCount = skipper.docCount(level);

            if (minDocID > docID) {
                docID = minDocID;
            }

            int intervalSize = maxDocID - minDocID + 1;
            double density = (docCount * 100.0) / intervalSize;

            table.addRow(
                String.valueOf(intervalNum),
                String.format("[%d-%d]", minDocID, maxDocID),
                String.format("%,d", intervalSize),
                minValue == maxValue ? String.valueOf(minValue) :
                                      String.format("[%d-%d]", minValue, maxValue),
                String.format("%,d", docCount),
                String.format("%.1f%%", density)
            );

            intervalNum++;
            docID = maxDocID + 1;
        }

        table.print();

        System.out.println("\nObservations:");
        System.out.println("- Intervals with same min/max value were extended beyond 4096 docs");
        System.out.println("- This happens when index is sorted and values are consecutive");
        System.out.println("- Density shows percentage of docs with values in that range");
    }
}
