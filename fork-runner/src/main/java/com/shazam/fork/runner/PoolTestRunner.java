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

import com.shazam.fork.model.Device;
import com.shazam.fork.model.DynamicPool;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;

import static com.shazam.fork.Utils.namedExecutor;

public class PoolTestRunner implements Runnable, DynamicPool.NewDeviceListener {
    private final Logger logger = LoggerFactory.getLogger(PoolTestRunner.class);
    public static final String DROPPED_BY = "DroppedBy-";

    private final Pool pool;
    private final Queue<TestCaseEvent> testCases;
    private final CountDownLatch poolCountDownLatch;
    private final DeviceTestRunnerFactory deviceTestRunnerFactory;
    private final ProgressReporter progressReporter;
    private Phaser phaser;
    private ExecutorService concurrentDeviceExecutor;
    private ExecutorService deviceMonitorExecutor;

    public PoolTestRunner(DeviceTestRunnerFactory deviceTestRunnerFactory, Pool pool,
                          Queue<TestCaseEvent> testCases,
                          CountDownLatch poolCountDownLatch,
                          ProgressReporter progressReporter) {
        this.pool = pool;
        this.testCases = testCases;
        this.poolCountDownLatch = poolCountDownLatch;
        this.deviceTestRunnerFactory = deviceTestRunnerFactory;
        this.progressReporter = progressReporter;
    }

    public void run() {
        String poolName = pool.getName();
        try {
            concurrentDeviceExecutor = namedExecutor("DeviceExecutor-%d");
            deviceMonitorExecutor = namedExecutor("deviceMonitorExecutor-%d");
            phaser = new Phaser(1);
            logger.info("Pool {} started", poolName);

            for (Device device : pool.getDevices()) {
                onNewDevice(device);
            }

            if (pool instanceof DynamicPool) {
                DynamicPool dynamicPool = (DynamicPool) this.pool;
                dynamicPool.registerNewDeviceListener(this);
                Runnable deviceMonitor = dynamicPool.createDeviceMonitor();
                deviceMonitorExecutor.execute(deviceMonitor);
            }

            phaser.arriveAndAwaitAdvance();
        } catch (Exception e) {
            logger.warn("Pool {} was interrupted while running", poolName);
        } finally {
            if (concurrentDeviceExecutor != null) {
                concurrentDeviceExecutor.shutdown();
            }
            if (deviceMonitorExecutor != null) {
                deviceMonitorExecutor.shutdown();
            }
            logger.info("Pool {} finished", poolName);
            poolCountDownLatch.countDown();
            logger.info("Pools remaining: {}", poolCountDownLatch.getCount());
        }
    }

    public void onNewDevice(Device device) {
        Runnable deviceTestRunner = deviceTestRunnerFactory.createDeviceTestRunner(pool, testCases,
                phaser, device, progressReporter);
        concurrentDeviceExecutor.execute(deviceTestRunner);
    }
}
