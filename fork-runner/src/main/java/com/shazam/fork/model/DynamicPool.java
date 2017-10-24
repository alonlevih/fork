package com.shazam.fork.model;

import com.shazam.fork.device.DeviceLoader;

import java.util.ArrayList;
import java.util.List;

public class DynamicPool extends Pool {

	private final DeviceLoader deviceLoader;

	public DynamicPool(String poolname, DeviceLoader deviceLoader) {
		super(poolname);
		this.deviceLoader = deviceLoader;
	}

	@Override
	public List<Device> getDevices() {
		return new ArrayList<>(deviceLoader.loadDevices().getDevices());
	}

}
