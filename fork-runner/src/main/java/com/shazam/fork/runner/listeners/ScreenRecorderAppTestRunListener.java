/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.runner.listeners;

import com.android.ddmlib.*;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.system.adb.CollectingShellOutputReceiver;
import com.shazam.fork.system.io.FileManager;
import com.shazam.fork.system.io.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.shazam.fork.system.io.FileType.SCREENRECORDAPP;

class ScreenRecorderAppTestRunListener implements ITestRunListener {
    private final FileManager fileManager;
    private final Pool pool;
    private final Device device;
    private final IDevice deviceInterface;
    private final Logger logger = LoggerFactory.getLogger(ScreenRecorderAppTestRunListener.class);

    private boolean hasFailed;

    public ScreenRecorderAppTestRunListener(FileManager fileManager, Pool pool, Device device) {
        this.fileManager = fileManager;
        this.pool = pool;
        this.device = device;
        deviceInterface = device.getDeviceInterface();
    }

    @Override
    public void testRunStarted(String runName, int testCount) {
    }


    private String createFilenameForTest(TestIdentifier testIdentifier, FileType fileType) {
        return String.format("%s.%s", testIdentifier.toString(), fileType.getSuffix());
    }

    @Override
    public void testStarted(TestIdentifier test) {
        hasFailed = false;
        try {
            CollectingShellOutputReceiver receiver = new CollectingShellOutputReceiver();
            deviceInterface.executeShellCommand(String.format("am start -a \"android.intent.action.videorecord.START\" -e outputFileName %s -n \"com.houzz.screenrecord/com.houzz.screenrecord.VideoRecordingActivity\"", createFilenameForTest(test, SCREENRECORDAPP)), receiver);
        } catch (TimeoutException e) {
            // logger.warn(e.toString())
        } catch (AdbCommandRejectedException e) {
            // logger.warn(e.toString())
        } catch (ShellCommandUnresponsiveException e) {
            // logger.warn(e.toString())
        } catch (IOException e) {
            // logger.warn(e.toString())
        };
    }

    @Override
    public void testFailed(TestIdentifier test, String trace) {
        hasFailed = true;
    }

    @Override
    public void testAssumptionFailure(TestIdentifier test, String trace) {
    }

    @Override
    public void testIgnored(TestIdentifier test) {
        testEnded(test, null);
    }

    @Override
    public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
        try {
            CollectingShellOutputReceiver receiver = new CollectingShellOutputReceiver();
            deviceInterface.executeShellCommand("am start -a \"android.intent.action.videorecord.STOP\" -n \"com.houzz.screenrecord/com.houzz.screenrecord.VideoRecordingActivity\"", receiver);

            if (hasFailed) {
                Thread.sleep(1000);
                File localVideoFile = fileManager.createFile(SCREENRECORDAPP, pool, device, test);
                deviceInterface.pullFile(String.format("/storage/emulated/0/android/data/com.houzz.screenrecord/cache/recordings/%s", createFilenameForTest(test, SCREENRECORDAPP)), localVideoFile.getAbsolutePath());
            }

            deviceInterface.executeShellCommand("rm -fr /storage/emulated/0/android/data/com.houzz.screenrecord/cache/recordings", receiver);
        } catch (Exception e) {
            // logger.warn(e.toString())
        } 
    }

    @Override
    public void testRunFailed(String errorMessage) {
    }

    @Override
    public void testRunStopped(long elapsedTime) {
    }

    @Override
    public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
    }
}
