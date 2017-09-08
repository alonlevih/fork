package com.shazam.fork.suite;

import org.jf.dexlib.ClassDefItem;

import java.util.regex.Pattern;

public class PackageAndClassNameMatcher implements TestClassMatcher {
    private final Pattern packagePattern;
    private final Pattern classPattern;

    public PackageAndClassNameMatcher(Pattern packagePattern, Pattern classPattern) {
        this.packagePattern = packagePattern;
        this.classPattern = classPattern;
    }

    @Override
    public boolean matchesPatterns(ClassDefItem typeDescriptor) {
        try {
            String packageName = getPackageName(typeDescriptor.getClassType().getTypeDescriptor());
            String className = getClassName(typeDescriptor.getClassType().getTypeDescriptor());
            return packagePattern.matcher(packageName).matches() && classPattern.matcher(className).matches();
        } catch (StringIndexOutOfBoundsException ignored) {
            return false;
        }

    }

    private String getClassName(String typeDescriptor) {
        int finalSlashIndex = getFinalSlashIndex(typeDescriptor);
        return typeDescriptor.substring(finalSlashIndex + 1, typeDescriptor.length() - 1);
    }

    private String getPackageName(String typeDescriptor) {
        int finalSlashIndex = getFinalSlashIndex(typeDescriptor);
        return typeDescriptor.substring(1, finalSlashIndex).replace('/', '.');
    }

    private int getFinalSlashIndex(String typeDescriptor) {
        return typeDescriptor.lastIndexOf('/');
    }
}
