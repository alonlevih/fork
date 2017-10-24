package com.shazam.fork.suite;

import com.shazam.fork.model.TestCaseEvent;

import java.util.regex.Pattern;

public class TestMethodMatcher {
    private final Pattern methodPattern;

    public TestMethodMatcher(Pattern methodPattern) {
        this.methodPattern = methodPattern;
    }

    public boolean matchesPatterns(TestCaseEvent testCaseEvent) {
        try {
            return methodPattern.matcher(testCaseEvent.getTestMethod()).matches();
        } catch (StringIndexOutOfBoundsException ignored) {
            return false;
        }

    }
}
