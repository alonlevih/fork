package com.shazam.fork.model;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.google.common.base.Objects;
import org.jf.dexlib.AnnotationItem;

import javax.annotation.Nonnull;

import java.util.Arrays;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SIMPLE_STYLE;

public class TestCaseEvent {

    private final String testMethod;
    private final String testClass;
    private final String[] annotations;
    private final boolean isIgnored;

    protected TestCaseEvent(String testMethod, String testClass, AnnotationItem[] annotations, boolean isIgnored) {
        this.testMethod = testMethod;
        this.testClass = testClass;
        this.isIgnored = isIgnored;
        this.annotations =  Arrays.stream(annotations).map(annotation ->  annotation.getEncodedAnnotation().annotationType.getTypeDescriptor()).toArray(String[]::new);
    }

    public TestCaseEvent(String testMethod, String testClass, String[] annotations, boolean isIgnored) {
        this.testMethod = testMethod;
        this.testClass = testClass;
        this.isIgnored = isIgnored;
        this.annotations =  annotations;
    }

    public static TestCaseEvent newTestCase(String testMethod, String testClass, AnnotationItem[] annotations, boolean isIgnored) {
        return new TestCaseEvent(testMethod, testClass, annotations, isIgnored);
    }

    public static TestCaseEvent newTestCase(@Nonnull TestIdentifier testIdentifier, boolean isIgnored) {
        return newTestCase(testIdentifier, new AnnotationItem[0], isIgnored);
    }

    public static TestCaseEvent newTestCase(@Nonnull TestIdentifier testIdentifier, AnnotationItem[] annotations, boolean isIgnored) {
        return new TestCaseEvent(testIdentifier.getTestName(), testIdentifier.getClassName(), annotations, isIgnored);
    }

    public static TestCaseEvent newTestCase(@Nonnull TestIdentifier testIdentifier, String[] annotations, boolean isIgnored) {
        return new TestCaseEvent(testIdentifier.getTestName(), testIdentifier.getClassName(), annotations, isIgnored);
    }

    public String getTestMethod() {
        return testMethod;
    }

    public String getTestClass() {
        return testClass;
    }

    public boolean isIgnored() {
        return isIgnored;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.testMethod, this.testClass);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TestCaseEvent)) {
            return false;
        }
        final TestCaseEvent other = (TestCaseEvent) obj;
        return Objects.equal(this.testMethod, other.testMethod)
                && Objects.equal(this.testClass, other.testClass);
    }

    @Override
    public String toString() {
        return reflectionToString(this, SIMPLE_STYLE);
    }

    public String[] getAnnotations() {
        return annotations;
    }
}
