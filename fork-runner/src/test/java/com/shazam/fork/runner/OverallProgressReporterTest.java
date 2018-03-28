package com.shazam.fork.runner;

import com.shazam.fork.model.*;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.model.Pool.Builder.aDevicePool;
import static com.shazam.fork.model.TestCaseEvent.newTestCase;
import static com.shazam.fork.runner.FakePoolTestCaseAccumulator.aFakePoolTestCaseAccumulator;
import static com.shazam.fork.runner.FakeProgressReporterTrackers.aFakeProgressReporterTrackers;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class OverallProgressReporterTest {

    @Rule public JUnitRuleMockery mockery = new JUnitRuleMockery();
    @Mock private PoolProgressTracker mockPoolProgressTracker;
    private final FakePoolTestCaseAccumulator fakeTestCasesAccumulator = aFakePoolTestCaseAccumulator();

    private final Device A_DEVICE = aDevice().build();
    private final Pool A_POOL = aDevicePool()
            .addDevice(A_DEVICE)
            .build();
    private final TestCaseEvent A_TEST_CASE = newTestCase("aTestMethod", "aTestClass", annotations, false, emptyList(), emptyMap());

    private OverallProgressReporter overallProgressReporter;

    @Test
    public void requestRetryIsAllowedIfFailedLessThanPermitted() throws Exception {
        fakeTestCasesAccumulator.thatAlwaysReturns(0);
        overallProgressReporter = new OverallProgressReporter(1, 1,
                aFakeProgressReporterTrackers().thatAlwaysReturns(mockPoolProgressTracker),
                fakeTestCasesAccumulator);

        mockery.checking(new Expectations() {{
            oneOf(mockPoolProgressTracker).trackTestEnqueuedAgain();
        }});

        overallProgressReporter.requestRetry(A_POOL, A_TEST_CASE);
    }

    @Test
    public void requestRetryIsNotAllowedIfFailedMoreThanPermitted() throws Exception {
        fakeTestCasesAccumulator.thatAlwaysReturns(2);
        overallProgressReporter = new OverallProgressReporter(1, 1,
                aFakeProgressReporterTrackers().thatAlwaysReturns(mockPoolProgressTracker),
                fakeTestCasesAccumulator);

        mockery.checking(new Expectations() {{
            never(mockPoolProgressTracker).trackTestEnqueuedAgain();
        }});

        overallProgressReporter.requestRetry(A_POOL, A_TEST_CASE);
    }

}
