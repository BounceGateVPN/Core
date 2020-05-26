package org.skunion.BounceGateVPN.Core.websocket;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.skunion.BounceGateVPN.Core.Usr.Usr;

import com.github.smallru8.Secure.LocalUsrData;
import com.github.smallru8.Secure.Log.Log;

public class Client_ws extends WebSocketClient{
	public boolean readyFlag;
	public LocalUsrData lud;
	public Client_ws(URI serverUri,LocalUsrData lud) {
		super(serverUri);
		readyFlag = false;
		this.lud = lud;
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		Log.printMsg("Core", Log.MsgType.info, "Open WS connection.");
		verifyUUID();
	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(ByteBuffer message) {
		// TODO Auto-generated method stub
		if(!readyFlag) {//Step.4接收 RSA(SessionKey)
			try {
				byte[] sessionKey = Usr.sec.decryption_RSA(message.array(), PrivateKeyFactory.createKey(lud.privateKey.getEncoded()));
				lud.setSessionKey(sessionKey);
				Log.printMsg("Core", Log.MsgType.info, "Setting session key.");
				sendSessionKeyPlus1(sessionKey);//step.5
				readyFlag = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.printMsg("Core", Log.MsgType.err, "Session key decryption error!!");
				Log.printMsg("Core", Log.MsgType.info, "Closing connection!!");
				this.close();
			}
		}else {//認證結束 封包接收
			
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		// TODO Auto-generated method stub
		Log.printMsg("Core", Log.MsgType.info, "Closing connection.");
	}

	@Override
	public void onError(Exception ex) {
		// TODO Auto-generated method stub
		Log.printMsg("Core", Log.MsgType.err, "Error.");
		ex.printStackTrace();
	}
	
	/**
	 * Step.3與server端做金鑰交換
	 * @return
	 */
	private void verifyUUID() {
		Log.printMsg("Core", Log.MsgType.info, "Sending UUID.");
		this.send(lud.UUID);
	}
	
	/**
	 * session key +1
	 * @param sessionKey
	 * @return
	 */
	private void sendSessionKeyPlus1(byte[] sessionKey) {
		BigInteger s1 = new BigInteger(1,sessionKey);
		s1.add(BigInteger.ONE);
		try {
			this.send(lud.encryption_AES(s1.toByteArray()));
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
