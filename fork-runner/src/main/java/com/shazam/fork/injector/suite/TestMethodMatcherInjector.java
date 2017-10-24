/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.injector.suite;

import com.shazam.fork.Configuration;
import com.shazam.fork.suite.PackageAndClassNameAndAnnotationMatcher;
import com.shazam.fork.suite.TestClassMatcher;
import com.shazam.fork.suite.TestMethodMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import static com.shazam.fork.injector.ConfigurationInjector.configuration;

class TestMethodMatcherInjector {
    private static final Logger log = LoggerFactory.getLogger(TestMethodMatcherInjector.class);

    private TestMethodMatcherInjector() {}

    static TestMethodMatcher testMethodMatcher() {
        Configuration configuration = configuration();
        log.info("Fork will try to find test methods in {} from your instrumentation APK.", configuration.getTestPackage());
        return new TestMethodMatcher(configuration.getTestMethodPattern());
    }

    private static Pattern compilePatternFor(String methodPattern) {
        return Pattern.compile(methodPattern.replace(".", "\\.") + ".*");
    }
}
