package org.skunion.BounceGateVPN.Core;

import java.util.HashMap;
import java.util.Map;

import org.skunion.BounceGateVPN.Core.Manager.SwitchManager;

public class Lists {

	/**
	 * 所有SwitchManager(每個SwitchManager控制一台Switch)
	 */
	public static Map<String,SwitchManager> switchLs = new HashMap<>();//SHA512(switch名稱),對應switch
	
}
