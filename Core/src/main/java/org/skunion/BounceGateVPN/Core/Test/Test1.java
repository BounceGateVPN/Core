package org.skunion.BounceGateVPN.Core.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.java_websocket.server.WebSocketServer;
import org.skunion.BounceGateVPN.Core.Lists;
import org.skunion.BounceGateVPN.Core.Manager.SwitchManager;
import org.skunion.BounceGateVPN.Core.websocket.WS_Server;

/**
 * Server test
 * @author smallru8
 *
 */
public class Test1 {
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Creating VirtualSwitch.");
		SwitchManager sw1 = new SwitchManager("TestSwitch1");
		sw1.startSwitch();
		System.out.println("Setting VirtualSwitch.");
		Lists.switchLs.put(sw1.SHA512_switchName, sw1);//完成Switch註冊
		
		System.out.println("Starting server.");
		WebSocketServer server = new WS_Server(new InetSocketAddress(8080));
		server.run();
		System.out.println("Server has started. press any key to stop");
		System.in.read();
		server.stop();
		System.out.println("Closing server.");
	}
}
