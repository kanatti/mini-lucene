package org.kanatti.learning.skipindex;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Generates realistic test data for DocValues Skip Index experiments.
 *
 * Creates two indexes:
 * 1. WITHOUT skip index (regular NumericDocValuesField)
 * 2. WITH skip index (using .indexedField())
 */
public class SkipIndexDataGenerator {

    private static final int NUM_DOCS = 1_000_000;  // 1 million docs

    public static void main(String[] args) throws IOException {
        Path baseDir = Paths.get("data/skipindex");
        Files.createDirectories(baseDir);

        System.out.println("Generating " + NUM_DOCS + " documents...\n");

        // Create index WITHOUT skip index
        Path withoutSkipIndexDir = baseDir.resolve("without-skip-index");
        deleteDirectory(withoutSkipIndexDir);
        Files.createDirectories(withoutSkipIndexDir);
        long timeWithout = indexData(withoutSkipIndexDir, false);

        // Create index WITH skip index
        Path withSkipIndexDir = baseDir.resolve("with-skip-index");
        deleteDirectory(withSkipIndexDir);
        Files.createDirectories(withSkipIndexDir);
        long timeWith = indexData(withSkipIndexDir, true);

        System.out.println("\nIndexing Complete!");
        System.out.println("---------------------------------------");
        System.out.printf("WITHOUT skip index: %d ms%n", timeWithout);
        System.out.printf("WITH skip index:    %d ms%n", timeWith);
        System.out.printf("Overhead:           %d ms (%.1f%%)%n",
            timeWith - timeWithout,
            ((timeWith - timeWithout) * 100.0) / timeWithout);
        System.out.println("---------------------------------------");

        // Print index sizes
        long sizeWithout = getDirectorySize(withoutSkipIndexDir);
        long sizeWith = getDirectorySize(withSkipIndexDir);
        System.out.printf("Index size WITHOUT: %d bytes (%.2f MB)%n",
            sizeWithout, sizeWithout / (1024.0 * 1024.0));
        System.out.printf("Index size WITH:    %d bytes (%.2f MB)%n",
            sizeWith, sizeWith / (1024.0 * 1024.0));
        System.out.printf("Skip index overhead: %d bytes (%.2f KB)%n",
            sizeWith - sizeWithout, (sizeWith - sizeWithout) / 1024.0);
    }

    private static long indexData(Path indexDir, boolean useSkipIndex) throws IOException {
        System.out.println("Indexing to: " + indexDir.getFileName() +
                         (useSkipIndex ? " (WITH skip index)" : " (WITHOUT skip index)"));

        // IMPORTANT: Create new Random with same seed for each index
        // This ensures both indexes have identical data
        Random random = new Random(42);

        IndexWriterConfig config = new IndexWriterConfig();
        config.setRAMBufferSizeMB(256); // Use more RAM for faster indexing

        // Sort index by year for optimal skip index performance
        config.setIndexSort(new Sort(new SortField("year", SortField.Type.LONG)));

        long startTime = System.currentTimeMillis();

        try (IndexWriter writer = new IndexWriter(FSDirectory.open(indexDir), config)) {
            for (int i = 0; i < NUM_DOCS; i++) {
                Document doc = new Document();

                // Generate realistic data patterns
                int year = generateYear(random);
                int price = generatePrice(random);
                int userId = i; // Sequential user IDs

                // Add ID field for reference
                doc.add(new StringField("id", String.valueOf(i), Field.Store.YES));

                // Add year field - this is what we'll query on
                if (useSkipIndex) {
                    doc.add(NumericDocValuesField.indexedField("year", year));
                    doc.add(SortedNumericDocValuesField.indexedField("price", price));
                } else {
                    doc.add(new NumericDocValuesField("year", year));
                    doc.add(new SortedNumericDocValuesField("price", price));
                }

                writer.addDocument(doc);

                if ((i + 1) % 100_000 == 0) {
                    System.out.printf("  Indexed %,d docs...%n", i + 1);
                }
            }

            writer.commit();
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("  Completed in %d ms%n%n", endTime - startTime);

        return endTime - startTime;
    }

    /**
     * Generate year with realistic distribution:
     * - 10% docs: 2020-2021 (old data)
     * - 60% docs: 2022-2023 (bulk of data)
     * - 30% docs: 2024-2025 (recent data)
     */
    private static int generateYear(Random random) {
        double rand = random.nextDouble();
        if (rand < 0.10) {
            return 2020 + random.nextInt(2); // 2020-2021
        } else if (rand < 0.70) {
            return 2022 + random.nextInt(2); // 2022-2023
        } else {
            return 2024 + random.nextInt(2); // 2024-2025
        }
    }

    /**
     * Generate price with power-law distribution (most products cheap, few expensive)
     */
    private static int generatePrice(Random random) {
        // Power law: most prices between 10-100, some up to 10000
        double rand = random.nextDouble();
        return (int) (10 + Math.pow(rand, 3) * 9990);
    }

    private static void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                .sorted((a, b) -> -a.compareTo(b)) // Reverse order for deletion
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }

    private static long getDirectorySize(Path directory) throws IOException {
        return Files.walk(directory)
            .filter(Files::isRegularFile)
            .mapToLong(path -> {
                try {
                    return Files.size(path);
                } catch (IOException e) {
                    return 0;
                }
            })
            .sum();
    }
}
