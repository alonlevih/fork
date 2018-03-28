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
package com.shazam.fork.runner;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.system.adb.Installer;

import javax.annotation.Nullable;

public class TestRunParameters {
	private final TestCaseEvent test;
	private final String testPackage;
	private final String testRunner;
	private final boolean isCoverageEnabled;
	private final IRemoteAndroidTestRunner.TestSize testSize;
	private final int testOutputTimeout;
	private final IDevice deviceInterface;
	private final String excludedAnnotation;
	private final Installer installer;
	private boolean autoGrantingPermissions;
	private String denyPermissionsAnnotation;
	private final String applicationPackage;

	public TestCaseEvent getTest() {
		return test;
	}

	public String getTestPackage() {
		return testPackage;
	}

	public String getTestRunner() {
		return testRunner;
	}

	@Nullable
	public IRemoteAndroidTestRunner.TestSize getTestSize() {
		return testSize;
	}

	public int getTestOutputTimeout() {
		return testOutputTimeout;
	}

	public IDevice getDeviceInterface() {
		return deviceInterface;
	}

	public boolean isCoverageEnabled(){
		return isCoverageEnabled;
	}

	public String getExcludedAnnotation() {
		return excludedAnnotation;
	}

	public Installer getInstaller() {
		return installer;
	}

	public boolean isAutoGrantingPermissions() {
		return autoGrantingPermissions;
	}

	public String getApplicationPackage() {
		return applicationPackage;
	}


	public String getDenyPermissionsAnnotation() {
		return denyPermissionsAnnotation;
	}

	public static class Builder {
		private TestCaseEvent test;
		private String testPackage;
		private String testRunner;
		private boolean isCoverageEnabled;
		private IRemoteAndroidTestRunner.TestSize testSize;
		private IDevice deviceInterface;
		private int testOutputTimeout;
		private String excludedAnnotation;
		private Installer installer;
		private boolean autoGrantPermissions;
		private String denyPermissionsAnnotation;
		private String applicationPackage;

		public static Builder testRunParameters() {
			return new Builder();
		}

		public Builder withTest(TestCaseEvent test) {
			this.test = test;
			return this;
		}

		public Builder withTestPackage(String testPackage) {
			this.testPackage = testPackage;
			return this;
		}

		public Builder withTestRunner(String testRunner) {
			this.testRunner = testRunner;
			return this;
		}

		public Builder withTestSize(IRemoteAndroidTestRunner.TestSize testSize) {
			this.testSize = testSize;
			return this;
		}

		public Builder withTestOutputTimeout(int testOutputTimeout) {
			this.testOutputTimeout = testOutputTimeout;
			return this;
		}

		public Builder withDeviceInterface(IDevice deviceInterface) {
			this.deviceInterface = deviceInterface;
			return this;
		}

		public Builder withCoverageEnabled(boolean isCoverageEnabled){
			this.isCoverageEnabled = isCoverageEnabled;
			return this;
		}

		public Builder withExcludedAnnotation(String excludedAnnotation) {
			this.excludedAnnotation = excludedAnnotation;
			return this;
		}

		public TestRunParameters build() {
			return new TestRunParameters(this);
		}

		public Builder withApplicationPackage(String applicationPackage) {
			this.applicationPackage = applicationPackage;
			return this;
		}


		public Builder withInstaller(Installer installer) {
			this.installer = installer;
			return this;
		}

		public Builder withAutoGrantPermissions(boolean autoGrantingPermissions) {
			this.autoGrantPermissions = autoGrantingPermissions;
			return this;
		}

		public Builder withDenyPermissionsAnnotation(String denyPermissionsAnnotation) {
			this.denyPermissionsAnnotation = denyPermissionsAnnotation;
			return this;
		}
	}

	private TestRunParameters(Builder builder) {
		test = builder.test;
		testPackage = builder.testPackage;
		testRunner = builder.testRunner;
		testSize = builder.testSize;
		testOutputTimeout = builder.testOutputTimeout;
		deviceInterface = builder.deviceInterface;
		isCoverageEnabled = builder.isCoverageEnabled;
		this.excludedAnnotation = builder.excludedAnnotation;
		this.denyPermissionsAnnotation = builder.denyPermissionsAnnotation;
		this.autoGrantingPermissions = builder.autoGrantPermissions;
		this.installer = builder.installer;
		this.applicationPackage = builder.applicationPackage;
	}
}
