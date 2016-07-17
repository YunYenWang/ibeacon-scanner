package com.cht.iot.bluez;

public class Beacon {
	public final String uuid;
	public final int major;
	public final int minor;
	public final int power;
	public final int rssi;
	
	public Beacon(byte[] rawdata, int offset) {
		uuid = hex(rawdata, offset, 16);
		major = toInteger(rawdata, offset + 16);
		minor = toInteger(rawdata, offset + 18);
		power = (int) rawdata[offset + 20];
		rssi = (int) rawdata[offset + 21];
	}
	
	protected String hex(byte[] bytes, int offset, int length) {
		int bound = offset + length;
		
		StringBuilder sb = new StringBuilder();
		for (int i = offset;i < bound;i++) {
			sb.append(String.format("%02X", bytes[i] & 0x0FF));
		}
		
		return sb.toString();
	}
	
	protected int toInteger(byte[] bytes, int offset) {
		return (bytes[offset] & 0x0FF) | ((bytes[offset + 1] & 0x0FF) >> 8);
	}
	
	protected double distance() {
		if (rssi == 0) {
			return -1.0d; // if we cannot determine accuracy, return -1.
		}

		double ratio = rssi * 1.0d / power;
		if (ratio < 1.0) {
			return Math.pow(ratio, 10d);
			
		} else {
			return (0.89976d * Math.pow(ratio, 7.7095d)) + 0.111d;
		}
	}
	
	@Override
	public String toString() {		
		return String.format("[%s] major: %d, minor: %d, power: %d dBm, rssi: %d dBm, %.2f m", uuid, major, minor, power, rssi, distance());
	}
}
