package kr.co.lean.mclient;

import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

import kr.co.lean.mclient.message.DefaultMessage;

public class SendThread extends Thread {

	private SocketInterface mSocketInterface;
	private LinkedBlockingQueue<DefaultMessage> mQueue;
	private OnSendListener mOnSendListener;

	public SendThread(SocketInterface socketInterface, OnSendListener l) {
		super("SendThread");
		mSocketInterface = socketInterface;
		mQueue = new LinkedBlockingQueue<DefaultMessage>();
		mOnSendListener = l;
	}
	
	public void send(DefaultMessage sendMessage) {
		mQueue.add(sendMessage);
	}

	@Override
	public void run() {
		super.run();
		while (!mSocketInterface.isClosed() && !isInterrupted()) {
			DefaultMessage sendMessage = null;
			try {
				sendMessage = mQueue.take();
				String message = sendMessage.getProtocol();
				Log.d("C->M", message);
				boolean success = mSocketInterface.writeProtocol(message);
				
				if (isInterrupted()) {
					break;
				}
				
				if (mOnSendListener != null) {
					mOnSendListener.onSend(success, sendMessage);
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (mOnSendListener != null) {
					mOnSendListener.onSend(false, sendMessage);
				}
			}
		}
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
		mOnSendListener = null;
	}
	
	public interface OnSendListener {
		void onSend(boolean success, DefaultMessage message);
	}
}