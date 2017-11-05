/*
 * Copyright 2016 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.runner.listeners;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.FailedTestCaseEvent;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.runner.ProgressReporter;
import com.shazam.fork.summary.TestCase;
import com.shazam.fork.summary.TestSuite;
import com.shazam.fork.system.io.FileManager;
import com.shazam.fork.system.io.FileType;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.shazam.fork.model.TestCaseEvent.newTestCase;

public class RetryListener extends NoOpITestRunListener {

    private static final Logger logger = LoggerFactory.getLogger(RetryListener.class);

    @Nonnull
    private final Device device;
    @Nonnull
    private final Queue<TestCaseEvent> queueOfTestsInPool;
    @Nonnull
    private final TestCaseEvent currentTestCaseEvent;
    private ProgressReporter progressReporter;
    private FileManager fileManager;
    private final Serializer serializer;
    private Pool pool;

    public RetryListener(@Nonnull Pool pool, @Nonnull Device device,
                         @Nonnull Queue<TestCaseEvent> queueOfTestsInPool,
                         @Nonnull TestCaseEvent currentTestCaseEvent,
                         @Nonnull ProgressReporter progressReporter,
                         FileManager fileManager) {
        checkNotNull(device);
        checkNotNull(queueOfTestsInPool);
        checkNotNull(currentTestCaseEvent);
        checkNotNull(progressReporter);
        checkNotNull(pool);
        this.device = device;
        this.queueOfTestsInPool = queueOfTestsInPool;
        this.currentTestCaseEvent = currentTestCaseEvent;
        this.progressReporter = progressReporter;
        this.pool = pool;
        this.fileManager = fileManager;
        serializer = new Persister();
    }
    
    @Override
    public void testFailed(TestIdentifier test, String trace) {
        progressReporter.recordFailedTestCase(pool, newTestCase(test, currentTestCaseEvent.getAnnotations(), false));

        if (progressReporter.requestRetry(pool, FailedTestCaseEvent.newTestCase(test, currentTestCaseEvent.getAnnotations(), false, trace))) {
            queueOfTestsInPool.add(currentTestCaseEvent);
            logger.info("Test " + test.toString() + " enqueued again into pool:" + pool.getName());
        } else {
            logger.info("Test " + test.toString() + " failed on device " + device.getSafeSerial() + " but retry is not allowed.");
        }
    }

    @Override
    public void testStarted(TestIdentifier testCase) {
        List<TestCase> testCases = new ArrayList<>(Arrays.asList(new TestCase(testCase.getTestName(), testCase.getClassName())));
        TestSuite suite = new TestSuite(testCases, new HashMap<>());
        suite.tests = suite.skipped = 1;
        suite.name = pool.getName();
        try {
            serializer.write(suite, fileManager.createFile(FileType.TEST, pool, device, testCase));
        } catch (Exception e) {
            // logger.warn(e.toString())
        }
    }

    public void removeFailureTraceFiles(TestIdentifier testCaseEvent) {
        final File file = fileManager.getFile(FileType.TEST, pool.getName(), device.getSafeSerial(), testCaseEvent);
        boolean deleted = file.delete();
        if(!deleted){
            logger.warn("Failed to remove file  " + file.getAbsoluteFile() + " for a failed but enqueued again test");
        }
    }
}
