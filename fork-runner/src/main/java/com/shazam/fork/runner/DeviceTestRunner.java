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

import com.android.ddmlib.*;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.*;
import com.shazam.fork.system.adb.Installer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Phaser;

import static com.shazam.fork.system.io.RemoteFileManager.*;

public class DeviceTestRunner implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(DeviceTestRunner.class);

    private final Installer installer;
    private final Pool pool;
    private final Device device;
    private final Queue<TestCaseEvent> queueOfTestsInPool;
    private final Phaser phaser;
    private final ProgressReporter progressReporter;
    private final TestRunFactory testRunFactory;

    public DeviceTestRunner(Installer installer,
                            Pool pool,
                            Device device,
                            Queue<TestCaseEvent> queueOfTestsInPool,
                            Phaser phaser,
                            ProgressReporter progressReporter,
                            TestRunFactory testRunFactory) {
        this.installer = installer;
        this.pool = pool;
        this.device = device;
        this.queueOfTestsInPool = queueOfTestsInPool;
        this.phaser = phaser;
        this.progressReporter = progressReporter;
        this.testRunFactory = testRunFactory;
    }

    @Override
    public void run() {
        phaser.register();
        if (tryInstall()) {
            TestCaseEvent testCaseEvent;
            while (device.getDeviceInterface().isOnline() && (testCaseEvent = queueOfTestsInPool.poll()) != null) {
                try {
                    TestRun testRun = testRunFactory.createTestRun(testCaseEvent,
                            installer,
                            device,
                            pool,
                            progressReporter,
                            queueOfTestsInPool);
                    testRun.execute();
                } catch (Exception ex) {
                    // logger.warn(e.toString());
                }
            }
        }

        unregisterDevice();
        logger.info("Device {} from pool {} finished", device.getSerial(), pool.getName());
    }

    private boolean tryInstall() {
        while (device.getDeviceInterface().isOnline()) {
            try {
                IDevice deviceInterface = device.getDeviceInterface();
                DdmPreferences.setTimeOut(30000);
                installer.prepareInstallation(deviceInterface);
                // For when previous run crashed/disconnected and left files behind
                removeRemoteDirectory(deviceInterface);
                createRemoteDirectory(deviceInterface);
                createCoverageDirectory(deviceInterface);
                clearLogcat(deviceInterface);
                return true;
            } catch (Exception ex) {
                // logger.warn(e.toString());
            }
        }
        return false;
    }

    private void unregisterDevice() {
        phaser.arriveAndDeregister();
        if (pool instanceof DynamicPool) {
            ((DynamicPool)pool).getActiveDevices().remove(device);
        }
    }

    private void clearLogcat(final IDevice device) {
        try {
            device.executeShellCommand("logcat -c", new NullOutputReceiver());
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
            logger.warn("Could not clear logcat on device: " + device.getSerialNumber(), e);
        }
    }
}
