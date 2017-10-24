/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.suite;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.ClassDefItem;

import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class PackageAndClassNameAndAnnotationMatcher extends PackageAndClassNameMatcher {

    private String testClassAnnotation;
    
    public PackageAndClassNameAndAnnotationMatcher(Pattern packagePattern, Pattern classPattern, String testClassAnnotation) {
        super(packagePattern, classPattern);
        this.testClassAnnotation = testClassAnnotation;
    }

    @Override
    public boolean matchesPatterns(ClassDefItem typeDescriptor) {
        if (testClassAnnotation != null) {
            if (typeDescriptor != null && typeDescriptor.getAnnotations() != null && hasRequiredAnnotation(typeDescriptor.getAnnotations())) {
                return super.matchesPatterns(typeDescriptor);
            } else {
                return false;
            }
        }

        return super.matchesPatterns(typeDescriptor);
    }

    private boolean hasRequiredAnnotation(AnnotationDirectoryItem annotationDirectoryItem) {
        AnnotationSetItem classAnnotations = annotationDirectoryItem.getClassAnnotations();
        if (classAnnotations == null) {
            return false;
        }
        return containsAnnotation(testClassAnnotation, classAnnotations.getAnnotations());
    }

    private boolean containsAnnotation(String comparisonAnnotation, AnnotationItem... annotations) {
        return asList(annotations).stream()
                .filter(annotation -> comparisonAnnotation.equals(stringType(annotation)))
                .findFirst()
                .isPresent();
    }

    private String stringType(AnnotationItem annotation) {
        return annotation.getEncodedAnnotation().annotationType.getTypeDescriptor();
    }
}

