package kr.co.lean.mclient;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import android.util.Log;

public class SocketInterface {
	
	public static final String TAG = SocketInterface.class.getSimpleName();
	
	private static final String SUFFIX = "\r\n";
	private static final char CR = '\r';
	private static final char NL = '\n';

	private String mHostIp;
	private int mPort;
	
	private SSLSocket mSocket;
	private OutputStream mOutputStream;
	private InputStream mInputStream;

	private Object lockRead = new Object();
	private Object lockWrite = new Object();

	private boolean mClosed = false;
	
	private OnSocketErrorListener mOnSocketErrorListener;

	public SocketInterface(String hostIp, int port, OnSocketErrorListener l) throws Exception {
		mHostIp = hostIp;
		mPort = port;
		try {
			SocketAddress addr = new InetSocketAddress(mHostIp, mPort);
			SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            mSocket = (SSLSocket) sslsocketfactory.createSocket();
            mSocket.connect(addr, 3000);
			mSocket.setReuseAddress(true);
			mSocket.setTcpNoDelay(true);

			mInputStream = mSocket.getInputStream();
			mOutputStream = mSocket.getOutputStream();
			
			mOnSocketErrorListener = l;
			Log.d(TAG, "connected to MServer");
		} catch (Exception e) {
			if (mSocket != null && !mSocket.isConnected())
				mSocket = null;
			e.printStackTrace();
			throw e;
		}
	}
	
	public boolean writeProtocol(String data) {
		boolean isSuccess = false;
		if (mOutputStream == null) {
			socketError(0);
			return isSuccess;
		}
		synchronized (lockWrite) {
			try {
				mOutputStream.write((data + SUFFIX).getBytes());
				mOutputStream.flush();
				isSuccess = true;
			} catch (Exception e) {
				e.printStackTrace();
				socketError(0);
			}
		}
		return isSuccess;
	}
	
	public String readProtocol() {
		if (mInputStream == null) {
			socketError(0);
			return null;
		}
		synchronized (lockRead) {
			StringBuilder sb = new StringBuilder();
			boolean inCR = false;
			try {
				char currentChar;
				while (true) {
					currentChar = (char) mInputStream.read();
					if (currentChar == (char) -1) {
						socketError(0);
						return null;
					}
					if (currentChar == CR) {
						inCR = true;
					} else if (inCR && currentChar == NL) {
						break;
					} else {
						inCR = false;
						sb.append(currentChar);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				socketError(0);
				return null;
			}
			return sb.toString();
		}
	}
	
	public void close() {
		if (isClosed()) {
			return;
		}
		
		try {
			mSocket.shutdownInput();
		} catch (Exception e) {
		}

		try {
			mSocket.shutdownOutput();
		} catch (Exception e) {
		}

		try {
			if (mInputStream != null)
				mInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (mOutputStream != null)
				mOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (mSocket != null)
				mSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		mSocket = null;
		mInputStream = null;
		mOutputStream = null;
		mClosed = true;
		
		Log.d(TAG, "socket is closed");
	}

	public boolean isClosed() {
		return mClosed;
	}
	
	private void socketError(int reason) {
		mOnSocketErrorListener.onSocketError(reason);
	}
	
	public interface OnSocketErrorListener {
		void onSocketError(int reason);
	}
}