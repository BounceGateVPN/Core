package org.skunion.BounceGateVPN.Core.websocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.net.ssl.SSLSession;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.enums.Opcode;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.framing.Framedata;
import org.skunion.BounceGateVPN.Core.Usr.connectUsrData;

/**
 * 重新封裝WS_Server的onMessage丟出的WebSocket
 * 主要原因為需要重載send，做加密
 * @author smallru8
 *
 */
public class WS_Case implements WebSocket{
	
	public boolean readyFlag = false;
	public WebSocket conn;//本體
	public connectUsrData ud;//加密用
	
	public WS_Case(WebSocket conn) {
		this.conn = conn;
	}
	
	@Override
	public void close(int code, String message) {
		// TODO Auto-generated method stub
		conn.close(code, message);
	}

	@Override
	public void close(int code) {
		// TODO Auto-generated method stub
		conn.close(code);
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		conn.close();
	}

	@Override
	public void closeConnection(int code, String message) {
		// TODO Auto-generated method stub
		conn.closeConnection(code, message);
	}

	@Override
	public void send(String text) {
		// TODO Auto-generated method stub
		conn.send(text);
	}

	@Override
	public void send(ByteBuffer bytes) {
		// TODO Auto-generated method stub
		conn.send(bytes);
	}

	@Override
	public void send(byte[] bytes) {//送出前加密
		// TODO Auto-generated method stub
		try {
			conn.send(ud.encryption_AES(bytes));
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void sendFrame(Framedata framedata) {
		// TODO Auto-generated method stub
		conn.sendFrame(framedata);
	}

	@Override
	public void sendFrame(Collection<Framedata> frames) {
		// TODO Auto-generated method stub
		conn.sendFrame(frames);
	}

	@Override
	public void sendPing() {
		// TODO Auto-generated method stub
		conn.sendPing();
	}

	@Override
	public void sendFragmentedFrame(Opcode op, ByteBuffer buffer, boolean fin) {
		// TODO Auto-generated method stub
		conn.sendFragmentedFrame(op, buffer, fin);
	}

	@Override
	public boolean hasBufferedData() {
		// TODO Auto-generated method stub
		return conn.hasBufferedData();
	}

	@Override
	public InetSocketAddress getRemoteSocketAddress() {
		// TODO Auto-generated method stub
		return conn.getLocalSocketAddress();
	}

	@Override
	public InetSocketAddress getLocalSocketAddress() {
		// TODO Auto-generated method stub
		return conn.getLocalSocketAddress();
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return conn.isOpen();
	}

	@Override
	public boolean isClosing() {
		// TODO Auto-generated method stub
		return conn.isClosing();
	}

	@Override
	public boolean isFlushAndClose() {
		// TODO Auto-generated method stub
		return conn.isFlushAndClose();
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return conn.isClosed();
	}

	@Override
	public Draft getDraft() {
		// TODO Auto-generated method stub
		return conn.getDraft();
	}

	@Override
	public ReadyState getReadyState() {
		// TODO Auto-generated method stub
		return conn.getReadyState();
	}

	@Override
	public String getResourceDescriptor() {
		// TODO Auto-generated method stub
		return conn.getResourceDescriptor();
	}

	@Override
	public <T> void setAttachment(T attachment) {
		// TODO Auto-generated method stub
		conn.setAttachment(attachment);
	}

	@Override
	public <T> T getAttachment() {
		// TODO Auto-generated method stub
		return conn.getAttachment();
	}

	@Override
	public boolean hasSSLSupport() {
		// TODO Auto-generated method stub
		return conn.hasSSLSupport();
	}

	@Override
	public SSLSession getSSLSession() throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return conn.getSSLSession();
	}

}
