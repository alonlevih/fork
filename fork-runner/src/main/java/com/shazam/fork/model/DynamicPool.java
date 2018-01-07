package com.shazam.fork.model;

import com.shazam.fork.device.DeviceLoader;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DynamicPool extends Pool {

	private final DeviceLoader deviceLoader;
	private NewDeviceListener newDeviceListener;
	private Set<Device> activeDevices;

	public DynamicPool(String poolname, DeviceLoader deviceLoader) {
		super(poolname, new ArrayList<>());
		this.deviceLoader = deviceLoader;
		this.activeDevices = new HashSet<>(devices);
	}

	public void registerNewDeviceListener(NewDeviceListener listener) {
		newDeviceListener = listener;
	}

	public DeviceMonitorRunner createDeviceMonitor() {
		return new DeviceMonitorRunner();
	}

	public void addDevice(Device device) {
		devices.add(device);
		activeDevices.add(device);
	}

	public Set<Device> getActiveDevices() {
		return activeDevices;
	}

	public class DeviceMonitorRunner implements Runnable {

		@Override
		public void run() {
			if (newDeviceListener != null) {
				while (true) {
					try {
						List<Device> devices = new ArrayList<>(deviceLoader.loadDevices().getDevices());
						List<Device> addedDevices = devices.stream().filter(elem -> !activeDevices.stream().anyMatch(d -> StringUtils.equals(d.getSerial(), elem.getSerial()))).collect(Collectors.toList());
						for (Device device : addedDevices.stream().filter(d -> d.getGeometry() != null).collect(Collectors.toList())) {
							newDeviceListener.onNewDevice(device);
							if (!DynamicPool.this.devices.stream().anyMatch(d -> StringUtils.equals(d.getSerial(), device.getSerial()))) {
								DynamicPool.this.devices.add(device);
							}
						}

						activeDevices = new HashSet<>(devices);
					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						pauseTillMonitoring();
					}
				}
			}
		}

		private void pauseTillMonitoring() {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ignored) {
			}
		}

	}

	public interface NewDeviceListener {
		void onNewDevice(Device device);
	}
}
