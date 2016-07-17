package com.cht.iot.bluez;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeaconScanner {
	static final Logger LOG = LoggerFactory.getLogger(BeaconScanner.class);
	
	static final String MAGIC_HEAD = "> ";
	static final int MAGIC_HEAD_LENGTH = MAGIC_HEAD.length();
	
	public BeaconScanner() {	
	}
	
	/*	
		root@raspberrypi:~/ibeacon-1.0/bin# hcidump --raw
		HCI sniffer - Bluetooth packet analyzer ver 5.23
		device: hci0 snap_len: 1500 filter: 0xffffffff
		> 04 3E 2A 02 01 00 00 E6 5E 58 F8 E6 A0 1E 02 01 06 1A FF 4C 
  	  	00 02 15 E3 43 42 82 1C E5 48 ED AF F2 AE 8F 45 62 32 FC 00 
  	  	00 00 00 D9 C4  	   
	 */
	
	public void scan() throws Exception {
		Process p = Runtime.getRuntime().exec(new String[] { "/usr/bin/hcidump", "--raw" });

		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String ln;

		// skip the banner and seek to head of the data
		while ((ln = br.readLine()) != null) {
			if (ln.startsWith(MAGIC_HEAD)) {
				break;
			}
		}
		
		if (ln == null) {
			throw new IOException("Failed to dump the bluetooth rawdata");
		}		
				
		StringBuilder sb = new StringBuilder(ln.substring(MAGIC_HEAD_LENGTH)); // shift
		
		while ((ln = br.readLine()) != null) {
			if (ln.startsWith(MAGIC_HEAD)) {
				process(sb.toString());
				
				sb.setLength(0); // clean buffer
				
				ln = ln.substring(MAGIC_HEAD_LENGTH); // next rawdata
			}
			
			sb.append(ln);
		}
	}
	
	protected byte[] hex2bytes(String hex) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		StringTokenizer st = new StringTokenizer(hex);		
		while (st.hasMoreTokens()) {
			baos.write(Integer.parseInt(st.nextToken(), 16));
		}
		
		return baos.toByteArray();
	}
	
	protected void process(String s) {
		try {
			byte[] b = hex2bytes(s);
			
			if ((b.length > 44) &&
				((b[18] & 0x0FF) == 0x0FF) && // flag - manufacturing defined 
				((b[19] & 0x0FF) == 0x04C) && ((b[20] & 0x0FF) == 0x000) && // apple
				((b[21] & 0x0FF) == 0x002) && // iBeacon AD indicator
				((b[22] & 0x0FF) == 0x015)) { // length
				
				Beacon beacon = new Beacon(b, 23);
				
				LOG.info(beacon.toString());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
//	protected byte[] part(byte[] bytes, int offset, int length) {
//		byte[] d = new byte[length];
//		System.arraycopy(bytes, offset, d, 0, length);
//		return d;
//	}
//	
//	protected void dump(byte[] bytes) {
//		for (byte b : bytes) {
//			System.out.printf("%02X ", b);
//		}
//		System.out.println();
//	}

	public static void main(String[] args) throws Exception {
		BeaconScanner bs = new BeaconScanner();
		bs.scan();
	}
}
