/*
 * Copyright 2014 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.summary;

import com.google.common.collect.Lists;
import com.shazam.fork.Configuration;
import com.shazam.fork.model.*;
import com.shazam.fork.runner.PoolTestRunner;
import com.shazam.fork.system.io.FileManager;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.summary.PoolSummary.Builder.aPoolSummary;
import static com.shazam.fork.summary.Summary.Builder.aSummary;
import static com.shazam.fork.summary.TestResult.Builder.aTestResult;

public class SummaryCompiler {

    private static final boolean STRICT = false;

    private final Configuration configuration;
    private final FileManager fileManager;
    private final Serializer serializer;

    public SummaryCompiler(Configuration configuration, FileManager fileManager) {
        this.configuration = configuration;
        this.fileManager = fileManager;
        serializer = new Persister();
    }

    Summary compileSummary(Collection<Pool> pools, Collection<TestCaseEvent> testCases) {
        Summary.Builder summaryBuilder = aSummary();
        for (Pool pool : pools) {
            PoolSummary poolSummary = compilePoolSummary(pool, summaryBuilder);
            addFailedTests(poolSummary.getTestResults(), summaryBuilder);
            summaryBuilder.addPoolSummary(poolSummary);
        }
        addIgnoredTests(testCases, summaryBuilder);
        summaryBuilder.withTitle(configuration.getTitle());
        summaryBuilder.withSubtitle(configuration.getSubtitle());

        return summaryBuilder.build();
    }

    private PoolSummary compilePoolSummary(Pool pool, Summary.Builder summaryBuilder) {
        PoolSummary.Builder poolSummaryBuilder = aPoolSummary().withPoolName(pool.getName());
        for (Device device : pool.getDevices()) {
            compileResultsForDevice(pool, poolSummaryBuilder, summaryBuilder, device);
        }
        Device watchdog = getPoolWatchdog(pool.getName());
        compileResultsForDevice(pool, poolSummaryBuilder, summaryBuilder, watchdog);
        PoolSummary summary = poolSummaryBuilder.build();
        return summary;
    }

    private void compileResultsForDevice(Pool pool, PoolSummary.Builder poolSummaryBuilder, Summary.Builder summaryBuilder, Device device) {
        File[] deviceResultFiles = fileManager.getTestFilesForDevice(pool, device);
        if (deviceResultFiles == null) {
            return;
        }
        for (File file : deviceResultFiles) {
            Collection<TestResult> testResult = parseTestResultsFromFile(file, device);
            poolSummaryBuilder.addTestResults(testResult);
        }
    }

    private Device getPoolWatchdog(String poolName) {
        return aDevice()
                .withSerial(PoolTestRunner.DROPPED_BY + poolName)
                .withManufacturer("Clumsy-" + poolName)
                .withModel("Clumsy=" + poolName)
                .build();
    }

    private void addIgnoredTests(Collection<TestCaseEvent> testCases, Summary.Builder summaryBuilder) {
        for (TestCaseEvent testCase : testCases) {
            if (testCase.isIgnored()) {
                summaryBuilder.addIgnoredTest(testCase.getTestClass() + ":" + testCase.getTestMethod());
            }
        }
    }

    private void addFailedTests(Collection<TestResult> testResults, Summary.Builder summaryBuilder) {
        for (TestResult testResult : testResults) {
            int totalFailureCount = testResult.getTotalFailureCount();
            if (totalFailureCount > 0) {
                if (totalFailureCount > configuration.getRetryPerTestCaseQuota()) {
                    String failedTest = totalFailureCount + " times " + testResult.getTestClass()
                            + "#" + testResult.getTestMethod();
                    summaryBuilder.addFailedTests(failedTest);
                } else {
                    String flakyTest = totalFailureCount + " times " + testResult.getTestClass()
                            + "#" + testResult.getTestMethod();
                    summaryBuilder.addFlakyTests(flakyTest);
                }
            }
        }
    }

    private Collection<TestResult> parseTestResultsFromFile(File file, Device device) {
        try {
            TestSuite testSuite = serializer.read(TestSuite.class, file, STRICT);
            Collection<TestCase> testCases = testSuite.getTestCase();
            List<TestResult> result  = Lists.newArrayList();
            if ((testCases == null)) {
                return defaultTestResult(file, device, "Test method was not run!");
            }

            for(TestCase testCase : testCases){
                TestResult testResult = getTestResult(device, testSuite, testCase);
                result.add(testResult);
            }
            return result;
        } catch (Exception e) {
            return defaultTestResult(file, device, e.getMessage());
        }
    }

    private Collection<TestResult> defaultTestResult(File file, Device device, String errorTrace) {
        String[] classAndMethodName = file.getName().split(Pattern.quote("#"));
        List<TestResult> result  = Lists.newArrayList();
        result.add(aTestResult()
                .withDevice(device)
                .withTestClass(classAndMethodName[0])
                .withTestMethod(classAndMethodName[1].replace(".xml", ""))
                .withErrorTrace(errorTrace)
                .build());
        return result;
    }

    private TestResult getTestResult(Device device, TestSuite testSuite, TestCase testCase) {
        TestResult.Builder testResultBuilder = aTestResult()
                .withDevice(device)
                .withTestClass(testCase.getClassname())
                .withTestMethod(testCase.getName())
                .withTimeTaken(testCase.getTime())
                .withErrorTrace(testCase.getError())
                .withFailureTrace(testCase.getFailure());
        if (testSuite.getProperties() != null) {
            testResultBuilder.withTestMetrics(testSuite.getProperties());
        }
        return testResultBuilder.build();
    }
}
