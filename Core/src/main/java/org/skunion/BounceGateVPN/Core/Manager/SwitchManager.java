package org.skunion.BounceGateVPN.Core.Manager;

import java.security.interfaces.RSAPublicKey;
import java.sql.SQLException;

import com.github.smallru8.BounceGateVPN.Switch.VirtualSwitch;
import com.github.smallru8.Secure.UserData.ServerData;
import com.github.smallru8.util.SHA;

/**
 * 1台Switch
 * ServerData
 * 1個WS server(可以沒有)
 * N個WS client(可以沒有)
 * 
 * SwitchManager()
 * startSwitch()
 * 
 * @author smallru8
 *
 */
public class SwitchManager {

	private VirtualSwitch vSwitch;
	public ServerData serverData;
	public String SHA512_switchName;
	//private ArrayList<connectUsrData> clientLs;
	
	public SwitchManager(String switchName) {
		vSwitch = new VirtualSwitch();
		//clientLs = new ArrayList<connectUsrData>();
		serverData = new ServerData(switchName);
		SHA512_switchName = SHA.SHA512(switchName);
	}
	
	public void startSwitch() {
		vSwitch.start();
	}
	
	public VirtualSwitch getSwitch() {
		return vSwitch;
	}
	
	public ServerData getServerData() {
		return serverData;
	}
	
	/**
	 * 使用者是否存在
	 * @param UUID
	 * @return
	 */
	public boolean verifyUUID(String UUID) {
		try {
			if(serverData.getSQL().getUserName(serverData.getSQLConn(), UUID)!=null){
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public String getUserName(String UUID) {
		try {
			return serverData.getSQL().getUserName(serverData.getSQLConn(), UUID);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public RSAPublicKey getPublicKey(String UUID) {
		try {
			return serverData.getSQL().getUserPublicKey(serverData.getSQLConn(), UUID);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
