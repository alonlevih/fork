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
package com.shazam.fork.system.adb;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import javax.annotation.Nonnull;

import static java.lang.String.format;

public class Installer {
    private static final Logger logger = LoggerFactory.getLogger(Installer.class);
	private final String applicationPackage;
	private final String instrumentationPackage;
	private final File apk;
	private final File testApk;
    private final boolean autoGrantPermissions;

	public Installer(String applicationPackage,
					 String instrumentationPackage,
					 File apk,
					 File testApk,
					 boolean  autoGrantPermissions) {
		this.applicationPackage = applicationPackage;
		this.instrumentationPackage = instrumentationPackage;
		this.apk = apk;
		this.testApk = testApk;
		this.autoGrantPermissions = autoGrantPermissions;
	}

	public void prepareInstallation(IDevice device) {
		String params = optionalAutoGrantPermissionFlag(device);
		prepareInstallation(device, params);
	}

	public void prepareInstallation(IDevice device, String installationParams) {
		reinstall(device, getApplicationPackage(), getApk(), installationParams);
		reinstall(device, getInstrumentationPackage(), getTestApk(), installationParams);
		grantMockLocationInMarshmallow(device, getApplicationPackage());
	}

	private void reinstall(final IDevice device, final String appPackage, final File appApk, String shouldAutoGrantPermissions) {
		final String message = format("Error while installing %s on %s", appPackage, device.getSerialNumber());
		tryThrice(true, message, () -> {
			try {
				logger.debug("Uninstalling {} from {}", appPackage, device.getSerialNumber());
				device.uninstallPackage(appPackage);
				logger.debug("Installing {} to {}", appPackage, device.getSerialNumber());
				device.installPackage(appApk.getAbsolutePath(), true, shouldAutoGrantPermissions);
			} catch (InstallException e) {
				throw new RuntimeException(message, e);
			}
        });
	}

	@Nonnull
	public String optionalAutoGrantPermissionFlag(IDevice device) {
		return isMarshmallowOrMore(device) && isAutoGrantPermissions() ? "-g" : "";
	}

	private boolean isMarshmallowOrMore(@Nonnull IDevice device){
		return device.getVersion().getApiLevel() >= 23;
	}

	private void tryThrice(boolean disaster, String message, Runnable runnable) {
		for (int attempt = 1;; ++attempt) {
			try {
				runnable.run();
				if (attempt > 1) {
                    logger.warn("Success after: " + message);
				}
				return;
			} catch (RuntimeException e) {
                logger.error("Attempt #{} failed: {}", attempt, message);
                if (attempt == 3) {
                    if (disaster) {
                        throw e;
                    }
                    return;
                }
            }
        }
    }

    private void grantMockLocationInMarshmallow(final IDevice device, final String appPackage) {
        if (isMarshmallowOrMore(device)) {
            CollectingShellOutputReceiver receiver = new CollectingShellOutputReceiver();
            String command = " appops set " + appPackage + " android:mock_location allow";
            try {
                device.executeShellCommand(command, receiver);
                logger.debug("When mock location granted for " + appPackage + " :" + receiver.getOutput());
            } catch (Exception e) {
                logger.warn("Error when executing " + command + " on " + device.getSerialNumber(), e);
            }
        }
    }

	public String getApplicationPackage() {
		return applicationPackage;
	}

	public String getInstrumentationPackage() {
		return instrumentationPackage;
	}

	public File getApk() {
		return apk;
	}

	public File getTestApk() {
		return testApk;
	}

	public boolean isAutoGrantPermissions() {
		return autoGrantPermissions;
	}
}
