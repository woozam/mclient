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

	private MClientManagerWorker mMClientManagerWorker;

	private MClientManager() {
		mMClientManagerWorker = new MClientManagerWorker(this);
	}

	public void setup(String deviceinfo, String uid, String cv, String atype, String id, String pw) {
		mMClientManagerWorker.setup(deviceinfo, uid, cv, atype, id, pw);
		reconnect();
	}

	public void sendMessage(DefaultMessage message) {
		mMClientManagerWorker.sendMessage(message, true);
	}

	public void keepAlive() {
		mMClientManagerWorker.sendMessage(new KeepAliveMessage(), false);
	}
	
	public void reconnect() {
		mMClientManagerWorker.reconnecct();
	}

	@Override
	public void onSend(boolean success, DefaultMessage message) {
		onLog(String.format("[C->M] %s %s", String.valueOf(success), message.toString()));
	}

	@Override
	public void onReceive(String message) {
		if (message != null && message.length() > 0) {
			onLog(String.format("[M->C] %s", message.toString()));
			char header = message.charAt(0);
			switch (header) {
			// ack
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
				mMClientManagerWorker.onReceiveMessage();
				break;
			default:
				mMClientManagerWorker.onReceiveMessage();
				break;
			}
		}
	}

	@Override
	public void onSocketError(int reason) {
		reconnect();
	}

	@Override
	public void onTimeout(DefaultMessage message) {
		reconnect();
	}
	
	public interface OnLogListener {
		void onLog(String log);
	}
	
	private OnLogListener mOnLogListener;
	
	public void setOnLogListener(OnLogListener l) {
		mOnLogListener = l;
	}
	
	private void onLog(String log) {
		if (mOnLogListener != null) {
			mOnLogListener.onLog(log);
		}
	}
}