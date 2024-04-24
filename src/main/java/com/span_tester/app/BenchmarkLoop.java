package com.span_tester.app;

import datadog.trace.api.Trace;
import org.openjdk.jmh.annotations.*;

public class BenchmarkLoop {
    @Trace
    public void trace() {

    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void testTLS() throws InterruptedException {
        for (int i = 0; i != 1000000; i++) {
            trace();
        }
    }
}
