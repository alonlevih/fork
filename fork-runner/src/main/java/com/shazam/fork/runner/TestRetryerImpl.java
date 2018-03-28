package com.shazam.fork.runner;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.FailedTestCaseEvent;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;

import java.util.Queue;

public class TestRetryerImpl implements TestRetryer {
    private final ProgressReporter progressReporter;
    private final Pool pool;
    private final Queue<TestCaseEvent> queueOfTestsInPool;

    public TestRetryerImpl(ProgressReporter progressReporter, Pool pool, Queue<TestCaseEvent> queueOfTestsInPool) {
        this.progressReporter = progressReporter;
        this.pool = pool;
        this.queueOfTestsInPool = queueOfTestsInPool;
    }

    @Override
    public boolean rescheduleTestExecution(TestIdentifier testIdentifier, TestCaseEvent testCaseEvent) {
        progressReporter.recordFailedTestCase(pool, new TestCaseEvent(testIdentifier));
        if (progressReporter.requestRetry(pool,  new FailedTestCaseEvent(testIdentifier.getTestName(), testIdentifier.getClassName(), testCaseEvent.isIgnored(), testCaseEvent.getAnnotations(), testCaseEvent.getPermissionsToRevoke(), testCaseEvent.getProperties(), ((FailedTestCaseEvent) testCaseEvent).getFailureTrace()))) {
            queueOfTestsInPool.add(testCaseEvent);
            return true;
        }
        return false;
    }
}
