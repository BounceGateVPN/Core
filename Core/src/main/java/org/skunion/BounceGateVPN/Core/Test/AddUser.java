package org.skunion.BounceGateVPN.Core.Test;

import java.io.IOException;

import org.skunion.BounceGateVPN.Core.Lists;
import org.skunion.BounceGateVPN.Core.Manager.SwitchManager;

import com.github.smallru8.Secure.UserData.ClientData;

public class AddUser {
	public static void main(String[] args) {
		/*
		System.out.println("Creating VirtualSwitch.");
		SwitchManager sw1 = new SwitchManager("TestSwitch1");
		sw1.startSwitch();
		System.out.println("Setting VirtualSwitch.");
		Lists.switchLs.put(sw1.SHA512_switchName, sw1);//完成Switch註冊
		
		System.out.println("Starting server.");
		*/
		System.out.println("Check user data.");
		ClientData cd;
		try {
			cd = new ClientData("TestClientSession");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done.");
		
	}
}
