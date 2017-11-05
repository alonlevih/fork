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
package com.shazam.fork.pooling;

import com.shazam.fork.DynamicPooling;
import com.shazam.fork.ManualPooling;
import com.shazam.fork.device.DeviceLoader;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Devices;
import com.shazam.fork.model.DynamicPool;
import com.shazam.fork.model.Pool;

import java.util.ArrayList;
import java.util.Collection;

import static com.shazam.fork.model.Pool.Builder.aDevicePool;
import static java.util.Map.Entry;

public class DynamicDevicePoolLoader implements DevicePoolLoader {
    private static final String DEFAULT_POOL_NAME = "all-devices";
    private final DynamicPooling dynamicPooling;
    private DeviceLoader deviceLoader;

    public DynamicDevicePoolLoader(DynamicPooling dynamicPooling, DeviceLoader deviceLoader) {
        this.dynamicPooling = dynamicPooling;
        this.deviceLoader = deviceLoader;
    }

	public Collection<Pool> loadPools(Devices devices) {
		Collection<Pool> pools = new ArrayList<>();
        DynamicPool defaultPoolBuilder = new DynamicPool(DEFAULT_POOL_NAME, deviceLoader);
        for (Device device : devices.getDevices()) {
            defaultPoolBuilder.addDevice(device);
        }
        pools.add(defaultPoolBuilder);
		return pools;
	}
}
