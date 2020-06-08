package org.skunion.BounceGateVPN.Core.Usr;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.skunion.BounceGateVPN.Core.websocket.Client_ws;

import com.github.smallru8.BounceGateVPN.device.Port;
import com.github.smallru8.Secure.LocalUsrData;
import com.github.smallru8.Secure.Secure;
import com.github.smallru8.driver.tuntap.TapDevice;

public class Usr {
	public static LocalUsrData lud;
	public static WebSocketClient client;
	public static Secure sec;
	public static TapDevice td;
	public static Port defaultPort;
	
	public static void initSecureCore() {
		sec = new Secure();
	}
	
	public static void startTap() {
		td = new TapDevice();
		td.startEthernetDev();
		
		defaultPort = new Port(client) {
			
		};
	}
	
	public static void linkStarter(URI uri) {
		WebSocketClient client = new Client_ws(uri,lud);
		client.connect();
	}
}
