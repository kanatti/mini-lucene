package org.kanatti.minilucene.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;

/*
 * Benchmark results:
 *
 * Benchmark                           (bitsPerValue)  (numValues)  Mode  Cnt  Score   Error  Units
 * CeilDivisionBenchmark.integerTrick               3          100  avgt    3  0.621 ± 0.059  ns/op
 * CeilDivisionBenchmark.integerTrick               3        10000  avgt    3  0.615 ± 0.028  ns/op
 * CeilDivisionBenchmark.integerTrick              12          100  avgt    3  0.618 ± 0.070  ns/op
 * CeilDivisionBenchmark.integerTrick              12        10000  avgt    3  0.619 ± 0.062  ns/op
 * CeilDivisionBenchmark.mathCeil                   3          100  avgt    3  0.699 ± 0.019  ns/op
 * CeilDivisionBenchmark.mathCeil                   3        10000  avgt    3  0.697 ± 0.023  ns/op
 * CeilDivisionBenchmark.mathCeil                  12          100  avgt    3  0.698 ± 0.021  ns/op
 * CeilDivisionBenchmark.mathCeil                  12        10000  avgt    3  0.701 ± 0.048  ns/op
 *
 * Integer trick is ~13% faster (~0.62 ns vs ~0.70 ns)
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 1, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class CeilDivisionBenchmark {

    @Param({"100", "10000"})
    private int numValues;

    @Param({"3", "12"})
    private int bitsPerValue;

    @Benchmark
    public void integerTrick(Blackhole bh) {
        int totalBits = bitsPerValue * numValues;
        bh.consume((totalBits + 7) / 8);
    }

    @Benchmark
    public void mathCeil(Blackhole bh) {
        int totalBits = bitsPerValue * numValues;
        bh.consume((int) Math.ceil(totalBits / 8.0));
    }
}
