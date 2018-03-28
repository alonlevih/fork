package com.shazam.fork.model;

import com.android.ddmlib.testrunner.TestIdentifier;
import com.google.common.base.Objects;

import org.jf.dexlib.AnnotationItem;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import java.util.Arrays;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SIMPLE_STYLE;

public class TestCaseEvent {

    private final String testMethod;
    private final String testClass;
    private final String[] annotations;
    private final boolean isIgnored;
    private final List<String> permissionsToRevoke;
    private final Map<String, String> properties;

    public TestCaseEvent(String testMethod, String testClass,  boolean isIgnored, AnnotationItem[] annotations,List<String> permissionsToRevoke,  Map<String, String> properties) {
        this.testMethod = testMethod;
        this.testClass = testClass;
        this.isIgnored = isIgnored;
        this.permissionsToRevoke = permissionsToRevoke;
        this.properties = properties;
        this.annotations =  Arrays.stream(annotations).map(annotation ->  annotation.getEncodedAnnotation().annotationType.getTypeDescriptor()).toArray(String[]::new);
    }

    public TestCaseEvent(String testMethod, String testClass,  boolean isIgnored, String[] annotations,List<String> permissionsToRevoke,  Map<String, String> properties) {
        this.testMethod = testMethod;
        this.testClass = testClass;
        this.isIgnored = isIgnored;
        this.permissionsToRevoke = permissionsToRevoke;
        this.properties = properties;
        this.annotations = annotations;
    }

    public TestCaseEvent(TestIdentifier testIdentifier) {
        this(testIdentifier.getTestName(), testIdentifier.getClassName(), false,
                new String[0], emptyList(), emptyMap());
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

    public List<String> getPermissionsToRevoke() {
        return permissionsToRevoke;
    }

    public Map<String, String> getProperties() {
        return properties;
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
