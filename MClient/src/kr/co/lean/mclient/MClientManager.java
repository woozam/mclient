package kr.co.lean.mclient;

import kr.co.lean.mclient.MClientManagerWorker.OnTimeoutListener;
import kr.co.lean.mclient.ReceiveThread.OnReceiveListener;
import kr.co.lean.mclient.SendThread.OnSendListener;
import kr.co.lean.mclient.SocketInterface.OnSocketErrorListener;
import kr.co.lean.mclient.message.AckMessage;
import kr.co.lean.mclient.message.DefaultMessage;
import kr.co.lean.mclient.message.InboxMessage;
import kr.co.lean.mclient.message.KeepAliveMessage;

public class MClientManager implements OnReceiveListener, OnSocketErrorListener, OnTimeoutListener, OnSendListener {

	public static MClientManager sMClientManager;

	public static MClientManager getInstance() {
		if (sMClientManager == null) {
			synchronized (MClientManager.class) {
				if (sMClientManager == null) {
					sMClientManager = new MClientManager();
				}
			}
		}
		return sMClientManager;
	}

	public static String deviceinfo = "A0";
	public static String uid = "JP000000000012";

	private MClientManagerWorker mMClientManagerWorker;

	private MClientManager() {
		mMClientManagerWorker = new MClientManagerWorker(this);
	}

	public void sendMessage(DefaultMessage message) {
		mMClientManagerWorker.sendMessage(message);
	}

	public void keepAlive() {
		sendMessage(new KeepAliveMessage());
	}

	@Override
	public void onSend(boolean success, DefaultMessage message) {
	}

	@Override
	public void onReceive(String message) {
		if (message != null && message.length() > 0) {
			char header = message.charAt(0);
			switch (header) {
			// 로그인 ack
			case 'A':
				AckMessage ackMessage = new AckMessage(message);
				mMClientManagerWorker.onReceiveAckMessage(ackMessage);
				break;
			case 'Z':
				// 수신함
				InboxMessage inboxMessage = new InboxMessage(message);
				mMClientManagerWorker.onReceiveInboxMessage(inboxMessage);
				break;
			case 'M':
				// message
				char subHeader = message.charAt(2);
				switch (subHeader) {
				case 'S':
					// stable
					break;
				case 'V':
					// volatile
					break;
				}
				break;
			}
		}
	}

	@Override
	public void onSocketError(int reason) {
		mMClientManagerWorker.reconnecct();
	}

	@Override
	public void onTimeout(DefaultMessage message) {
		mMClientManagerWorker.reconnecct();
	}
}