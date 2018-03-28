package com.shazam.fork.model;

import com.android.ddmlib.testrunner.TestIdentifier;
import org.jf.dexlib.AnnotationItem;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FailedTestCaseEvent extends TestCaseEvent {

    private final String failureTrace;

    public FailedTestCaseEvent(String testMethod, String testClass, boolean isIgnored, String[] annotations, List<String> permissionsToRevoke, Map<String, String> properties, String failedTrace) {
        super(testMethod, testClass, isIgnored, annotations, permissionsToRevoke, properties);
        this.failureTrace = failedTrace;
    }

    public String getFailureTrace() {
        return failureTrace;
    }

    public static FailedTestCaseEvent newTestCase(TestIdentifier testIdentifier, String[] annotations, boolean isIgnored, String failedTrace) {
        return new FailedTestCaseEvent(testIdentifier.getTestName(), testIdentifier.getClassName(), isIgnored, annotations, new ArrayList<>(), new HashMap<>(), failedTrace);
    }

}
