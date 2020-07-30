package org.skunion.BounceGateVPN.Core.websocket;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.skunion.BounceGateVPN.Core.Lists;
import org.skunion.BounceGateVPN.Core.Manager.SwitchManager;
import org.skunion.BounceGateVPN.Core.Usr.connectUsrData;

import com.github.smallru8.Secure.Secure;
import com.github.smallru8.Secure.KeyGen.AES;
import com.github.smallru8.Secure.Log.Log;
import com.github.smallru8.util.Pair;

public class WS_Server extends WebSocketServer{

	private TimerTask task;//定時清空太久沒回應的ws client
	private Timer timer;
	private ArrayList<Pair<WebSocket,Integer>> waitingQ;
	
	private Map<WebSocket,WS_Case> CaseRecord;//conn對應的Case,註冊switch時用的是WS_Case
	private Map<WebSocket,SwitchManager> swLs;//conn對應的SwitchManager
	
	public WS_Server(InetSocketAddress address) {
		super(address);
		this.setConnectionLostTimeout(60);
		swLs = new HashMap<>();
		CaseRecord = new HashMap<>();
		waitingQ = new ArrayList<>();
		
		task = new TimerTask() {//定時作業
			@Override
			public void run() {
		        if(!waitingQ.isEmpty()) {
		        	Iterator<Pair<WebSocket,Integer>> it = waitingQ.iterator();
		        	while (it.hasNext()) {
		        		Pair<WebSocket,Integer> p = it.next();
		        		p.second++;
		        		if(p.second > 20)//超過20s
		        			p.first.close();//關連線
		        		it.remove();//從waitingQ中刪除
		        	}
		        }
			}
		};
		timer = new Timer();//開始計時
		timer.schedule(task, 1000, 1000);// 1s
	}
	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		Log.printMsg("Core-WS_Server", Log.MsgType.info, "IP : " + conn.getRemoteSocketAddress().getHostString() + ":" + conn.getRemoteSocketAddress().getPort() + " connected.");
		Pair<WebSocket,Integer> p = new Pair<WebSocket,Integer>();
		p.makePair(conn, 0);
		waitingQ.add(p);//client連入，加入等待清單
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		// TODO Auto-generated method stub
		Log.printMsg("Core-WS_Server", Log.MsgType.info,"IP : " + conn.getRemoteSocketAddress().getHostString() + ":" + conn.getRemoteSocketAddress().getPort() + ", closing connection.");
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		if(!swLs.containsKey(conn)) {//SHA512(switchName)
			if(Lists.switchLs.containsKey(message)) {//存在此switch
				swLs.put(conn, Lists.switchLs.get(message));//
			}else {
				removeFromWaitingQ(conn);
				conn.close();//不存在此switch
			}
		}else {//UUID
			//removeFromWaitingQ(conn);
			if(!swLs.get(conn).verifyUUID(message)){//UUID不存在
				removeFromWaitingQ(conn);
				swLs.remove(conn);
				conn.close();
			}else {//UUID存在
				WS_Case ws_case = new WS_Case(conn);//建Case
				ws_case.ud = new connectUsrData(message);//放UUID
				ws_case.ud.Name = swLs.get(conn).getUserName(message);//查Name
				ws_case.ud.publicKey = swLs.get(conn).getPublicKey(message);//查PublicKey
				byte[] sessionKey = null;
				try {
					sessionKey = AES.KeytoByteArray(new AES(256).AESKeyGen());//產生SessionKey
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ws_case.ud.setSessionKey(sessionKey);//設定sessionKey
				CaseRecord.put(conn, ws_case);//放進Case list
				try {//送出RSA2048(sessionKey)
					ws_case.conn.send(Secure.encryption_RSA(sessionKey, PublicKeyFactory.createKey(ws_case.ud.publicKey.getEncoded())));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void onMessage(WebSocket conn, ByteBuffer message) {
		if(!CaseRecord.get(conn).readyFlag) {//驗證sessionKey+1
			removeFromWaitingQ(conn);
			CaseRecord.get(conn).readyFlag = true;
			try {
				//驗證sessionKey+1
				if(verifySessionKeyPlus1(CaseRecord.get(conn).ud.sessionKey,CaseRecord.get(conn).ud.decryption_AES(message.array()))) {
					CaseRecord.get(conn).ud.sport = swLs.get(conn).getSwitch().addDevice(CaseRecord.get(conn));//註冊到Switch
				}else {//沒通過
					CaseRecord.remove(conn);
					swLs.remove(conn);
					conn.close();
				}
			} catch (IllegalBlockSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {//一般封包接收
			try {//送到註冊的Switch
				CaseRecord.get(conn).ud.sport.sendToVirtualDevice(CaseRecord.get(conn).ud.decryption_AES(message.array()));
			} catch (IllegalBlockSizeException | BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		// TODO Auto-generated method stub
		Log.printMsg("Core-WS_Server", Log.MsgType.err, "Error.\n" + ex.toString());
		//ex.printStackTrace();
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		Log.printMsg("Core-WS_Server", Log.MsgType.info, "Start WS server at port " + this.getPort() + ".");
	}

	private void removeFromWaitingQ(WebSocket conn) {
		Iterator<Pair<WebSocket,Integer>> it = waitingQ.iterator();
    	while (it.hasNext()) {
    		Pair<WebSocket,Integer> p = it.next();
    		if(p.first==conn) {
    			it.remove();//從waitingQ中刪除
    			break;
    		}
    	}
	}
	
	private boolean verifySessionKeyPlus1(byte[] sessionK,byte[] sessionKp1) {
		BigInteger s1 = new BigInteger(1,sessionK);
		s1.add(BigInteger.ONE);
		BigInteger s2 = new BigInteger(1,sessionKp1);
		return s1.equals(s2);
	}
	
}
