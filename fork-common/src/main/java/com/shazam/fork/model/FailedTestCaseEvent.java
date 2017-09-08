package com.shazam.fork.model;

import com.android.ddmlib.testrunner.TestIdentifier;
import org.jf.dexlib.AnnotationItem;

import javax.annotation.Nonnull;

public class FailedTestCaseEvent extends TestCaseEvent {

    private final String failureTrace;

    public FailedTestCaseEvent(String testName, String className, String[] annotations, boolean isIgnored, String failedTrace) {
        super(testName, className, annotations, isIgnored);
        this.failureTrace = failedTrace;
    }

    public String getFailureTrace() {
        return failureTrace;
    }

    public static FailedTestCaseEvent newTestCase(TestIdentifier testIdentifier, String[] annotations, boolean isIgnored, String failedTrace) {
        return new FailedTestCaseEvent(testIdentifier.getTestName(), testIdentifier.getClassName(), annotations, isIgnored, failedTrace);
    }

}
