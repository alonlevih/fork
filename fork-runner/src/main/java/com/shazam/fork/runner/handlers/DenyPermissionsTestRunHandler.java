/*
 * Copyright 2014 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.runner.handlers;

import com.android.ddmlib.IDevice;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.runner.TestRunParameters;
import com.shazam.fork.system.adb.Installer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class DenyPermissionsTestRunHandler {
    private static final Logger logger = LoggerFactory.getLogger(DenyPermissionsTestRunHandler.class);
    private final String denyPermissionsAnnotationCls;
    private TestCaseEvent testCase;
    private final IDevice device;
    private final Installer installer;

    public DenyPermissionsTestRunHandler(IDevice device, Installer installer, String denyPermissionsAnnotation, TestCaseEvent testCase) {
        this.device = device;
        this.installer = installer;
        this.denyPermissionsAnnotationCls = denyPermissionsAnnotation;
        this.testCase = testCase;
    }

    public DenyPermissionsTestRunHandler(TestRunParameters testRunParameters) {
        this(testRunParameters.getDeviceInterface(), testRunParameters.getInstaller(), testRunParameters.getDenyPermissionsAnnotation(), testRunParameters.getTest());
    }

    public void testRunStarted() {
        if (shouldDenyPermissions()) {
            installer.prepareInstallation(device, "");
        }
	}

	public void testRunEnded() {
        if (shouldDenyPermissions()) {
            installer.prepareInstallation(device);
        }
	}

    public boolean shouldDenyPermissions() {
        for (String annotation : testCase.getAnnotations()) {
            if (annotation.equals(denyPermissionsAnnotationCls)) {
                return true;
            }
        }

        return false;
    }
}
