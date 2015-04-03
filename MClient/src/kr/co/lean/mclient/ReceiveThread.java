package kr.co.lean.mclient;

import android.util.Log;

public class ReceiveThread extends Thread {

	private SocketInterface mSocketInterface;
	private OnReceiveListener mOnReceiveListener;

	public ReceiveThread(SocketInterface socketInterface, OnReceiveListener l) {
		super("ReceiveThread");
		mSocketInterface = socketInterface;
		mOnReceiveListener = l;
	}

	@Override
	public void run() {
		super.run();
		while (!mSocketInterface.isClosed() && !isInterrupted()) {
			try {
				String message = mSocketInterface.readProtocol();
				if (message != null) {
					Log.d("M->C", message);
				}
				
				if (isInterrupted()) {
					break;
				}
				
				if (mOnReceiveListener != null) {
					mOnReceiveListener.onReceive(message);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
		mOnReceiveListener = null;
	}
	
	public interface OnReceiveListener {
		void onReceive(String message);
	}
}