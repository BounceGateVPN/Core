package org.skunion.BounceGateVPN.Core.websocket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.github.smallru8.BounceGateVPN.device.Port;
import com.github.smallru8.Secure.Secure;
import com.github.smallru8.Secure.Log.Log;
import com.github.smallru8.Secure.UserData.ClientData;

public class WS_Client extends WebSocketClient {

	public boolean readyFlag;
	public ClientData clientData;
	public Port sport; //Switch port
	
	public WS_Client(ClientData clientData) throws URISyntaxException {
		super(new URI(clientData.IPaddr + ":" + clientData.port));
		this.clientData = clientData;
		readyFlag = false;
	}

	/**
	 * 跟Switch註冊後拿到的port
	 * @param port
	 */
	public void setPort(Port port) {
		this.sport = port;
	}
	
	//tuntap >> local switch >> WS_Client
	//給local switch送資料出去
	@Override
	public void send(byte[] data) {
		try {
			//加密後送出資料
			super.send(clientData.encryption_AES(data));
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onOpen(ServerHandshake handshakedata) {
		// TODO Auto-generated method stub
		Log.printMsg("Core-WS_Client", Log.MsgType.info, "Open WS connection.");
		try {
			keyEx1();
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onMessage(ByteBuffer message) {
		if(!readyFlag) {//驗證
			keyEx2(message.array());
			readyFlag = true;
		}else {//封包收發
			try {
				sport.sendToVirtualDevice(clientData.decryption_AES(message.array()));
			} catch (IllegalBlockSizeException | BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		// TODO Auto-generated method stub
		Log.printMsg("Core-WS_Client", Log.MsgType.info, "Closing connection.");
	}

	@Override
	public void onError(Exception ex) {
		// TODO Auto-generated method stub
		Log.printMsg("Core-WS_Client", Log.MsgType.err, "Error.");
		ex.printStackTrace();
	}
	
	/**
	 * 送出格式皆為String
	 * 送出 SHA512(switch name)
	 * 送出 UUID
	 * @throws NoSuchAlgorithmException 
	 * @throws UnsupportedEncodingException 
	 */
	private void keyEx1() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		MessageDigest digest = MessageDigest.getInstance("SHA-512");
		digest.reset();
		digest.update(clientData.destSwitchName.getBytes("utf-8"));
		String destSwitchName_SHA512 = String.format("%0128x", new BigInteger(1, digest.digest()));
		this.send(destSwitchName_SHA512);//送目標switch名稱
		this.send(clientData.UUID);
	}
	
	/**
	 * 接收RSA2048(SessionKey)
	 * @param data
	 */
	private void keyEx2(byte[] data) {
		byte[] sessionKey = null;
		try {
			sessionKey = Secure.decryption_RSA(data, PrivateKeyFactory.createKey(clientData.getPrivateKey().getEncoded()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.printMsg("Core-WS_Client", Log.MsgType.err, "Session key decryption error!!");
			Log.printMsg("Core-WS_Client", Log.MsgType.info, "Closing connection!!");
			this.close();
		}
		clientData.setSessionKey(sessionKey);
		//加1後送回
		BigInteger s1 = new BigInteger(1,sessionKey);
		s1.add(BigInteger.ONE);
		send(s1.toByteArray());
	}

}
