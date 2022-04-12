package com.span_tester.app;

import java.io.File;
import java.io.IOException;
import datadog.trace.api.Trace;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkLoop 
{
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
