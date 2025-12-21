package org.kanatti.learning.skipindex;

import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.FSDirectory;
import org.kanatti.common.TablePrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Comprehensive benchmark comparing skip index performance
 * with different collectors and selectivities.
 */
public class SkipIndexBenchmark {

    private static final int WARMUP_RUNS = 3;
    private static final int BENCHMARK_RUNS = 10;

    public static void main(String[] args) throws IOException {
        Path withoutSkipIndexDir = Paths.get("data/skipindex/without-skip-index");
        Path withSkipIndexDir = Paths.get("data/skipindex/with-skip-index");

        QueryTest[] queries = {
            new QueryTest("Very selective (5% of docs)", "year", 2020, 2020),
            new QueryTest("Low selectivity (30% of docs)", "year", 2024, 2025),
            new QueryTest("Medium selectivity (60% of docs)", "year", 2022, 2023),
            new QueryTest("High selectivity (95% of docs)", "year", 2021, 2025),
            new QueryTest("Exact match", "year", 2023, 2023),
        };

        // Run all benchmarks first (triggers INFO logs)
        TablePrinter countTable = new TablePrinter("Query", "Without", "With", "Speedup");
        for (QueryTest queryTest : queries) {
            BenchResult countWithout = runCountBenchmark(withoutSkipIndexDir, queryTest);
            BenchResult countWith = runCountBenchmark(withSkipIndexDir, queryTest);
            double speedup = countWithout.avgMs / countWith.avgMs;
            countTable.addRow(
                queryTest.description,
                String.format("%.2f ms", countWithout.avgMs),
                String.format("%.2f ms", countWith.avgMs),
                String.format("%.1fx", speedup)
            );
        }

        TablePrinter histTable = new TablePrinter("Query", "Without", "With", "Speedup");
        for (QueryTest queryTest : queries) {
            BenchResult histWithout = runHistogramBenchmark(withoutSkipIndexDir, queryTest);
            BenchResult histWith = runHistogramBenchmark(withSkipIndexDir, queryTest);
            double speedup = histWithout.avgMs / histWith.avgMs;
            histTable.addRow(
                queryTest.description,
                String.format("%.2f ms", histWithout.avgMs),
                String.format("%.2f ms", histWith.avgMs),
                String.format("%.1fx", speedup)
            );
        }

        TablePrinter avgTable = new TablePrinter("Query", "Without", "With", "Speedup");
        for (QueryTest queryTest : queries) {
            BenchResult avgWithout = runAverageBenchmark(withoutSkipIndexDir, queryTest);
            BenchResult avgWith = runAverageBenchmark(withSkipIndexDir, queryTest);
            double speedup = avgWithout.avgMs / avgWith.avgMs;
            avgTable.addRow(
                queryTest.description,
                String.format("%.2f ms", avgWithout.avgMs),
                String.format("%.2f ms", avgWith.avgMs),
                String.format("%.1fx", speedup)
            );
        }

        // Now print all results
        System.out.println("\nTotalHitCountCollector (no value reads)");
        countTable.print();

        System.out.println("\nHistogramCollector (reads all year values)");
        histTable.print();

        System.out.println("\nAverageAccumulator (reads all price values)");
        avgTable.print();
    }

    private static BenchResult runCountBenchmark(Path indexDir, QueryTest queryTest)
            throws IOException {
        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexDir))) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = SortedNumericDocValuesField.newSlowRangeQuery(
                queryTest.field, queryTest.min, queryTest.max);

            // Warmup
            for (int i = 0; i < WARMUP_RUNS; i++) {
                TotalHitCountCollector collector = new TotalHitCountCollector();
                searcher.search(query, collector);
            }

            // Benchmark
            long totalTime = 0;
            int totalHits = 0;
            for (int i = 0; i < BENCHMARK_RUNS; i++) {
                TotalHitCountCollector collector = new TotalHitCountCollector();
                long start = System.nanoTime();
                searcher.search(query, collector);
                totalTime += System.nanoTime() - start;
                totalHits = collector.getTotalHits();
            }

            double avgTimeMs = totalTime / (double) BENCHMARK_RUNS / 1_000_000.0;
            String label = indexDir.getFileName().toString().contains("without")
                         ? "WITHOUT skip index"
                         : "WITH skip index";
            return new BenchResult(label, avgTimeMs, totalHits);
        }
    }

    private static BenchResult runHistogramBenchmark(Path indexDir, QueryTest queryTest)
            throws IOException {
        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexDir))) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = SortedNumericDocValuesField.newSlowRangeQuery(
                queryTest.field, queryTest.min, queryTest.max);

            // Warmup
            for (int i = 0; i < WARMUP_RUNS; i++) {
                HistogramCollector collector = new HistogramCollector(queryTest.field);
                searcher.search(query, collector);
            }

            // Benchmark
            long totalTime = 0;
            int totalHits = 0;
            for (int i = 0; i < BENCHMARK_RUNS; i++) {
                HistogramCollector collector = new HistogramCollector(queryTest.field);
                long start = System.nanoTime();
                searcher.search(query, collector);
                totalTime += System.nanoTime() - start;
                totalHits = collector.getTotalHits();
            }

            double avgTimeMs = totalTime / (double) BENCHMARK_RUNS / 1_000_000.0;
            String label = indexDir.getFileName().toString().contains("without")
                         ? "WITHOUT skip index"
                         : "WITH skip index";
            return new BenchResult(label, avgTimeMs, totalHits);
        }
    }

    private static BenchResult runAverageBenchmark(Path indexDir, QueryTest queryTest)
            throws IOException {
        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexDir))) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = SortedNumericDocValuesField.newSlowRangeQuery(
                queryTest.field, queryTest.min, queryTest.max);

            // Warmup
            for (int i = 0; i < WARMUP_RUNS; i++) {
                AverageAccumulator collector = new AverageAccumulator("price");
                searcher.search(query, collector);
            }

            // Benchmark
            long totalTime = 0;
            int totalHits = 0;
            for (int i = 0; i < BENCHMARK_RUNS; i++) {
                AverageAccumulator collector = new AverageAccumulator("price");
                long start = System.nanoTime();
                searcher.search(query, collector);
                totalTime += System.nanoTime() - start;
                totalHits = collector.getCount();
            }

            double avgTimeMs = totalTime / (double) BENCHMARK_RUNS / 1_000_000.0;
            String label = indexDir.getFileName().toString().contains("without")
                         ? "WITHOUT skip index"
                         : "WITH skip index";
            return new BenchResult(label, avgTimeMs, totalHits);
        }
    }


    static class QueryTest {
        String description;
        String field;
        long min;
        long max;

        QueryTest(String description, String field, long min, long max) {
            this.description = description;
            this.field = field;
            this.min = min;
            this.max = max;
        }
    }

    static class BenchResult {
        String label;
        double avgMs;
        int totalHits;

        BenchResult(String label, double avgMs, int totalHits) {
            this.label = label;
            this.avgMs = avgMs;
            this.totalHits = totalHits;
        }
    }
}
