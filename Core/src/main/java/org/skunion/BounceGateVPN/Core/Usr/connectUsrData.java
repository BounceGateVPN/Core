package org.skunion.BounceGateVPN.Core.Usr;

import java.security.interfaces.RSAPublicKey;

import com.github.smallru8.BounceGateVPN.device.Port;
import com.github.smallru8.Secure.UserData.UsrData;

/**
 * 給SwitchManager紀錄client資料用
 * @author smallru8
 *
 */
public class connectUsrData extends UsrData{
	
	public Port sport;
	public RSAPublicKey publicKey;
	public byte[] sessionKey;
	
	public connectUsrData(String UUID) {
		this.UUID = UUID;
	}
	
	@Override
	public void setSessionKey(byte[] key) {
		sessionKey = key;
		super.setSessionKey(key);
	}
	
}
