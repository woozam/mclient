package kr.co.lean.mclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.lean.mclient.message.AckMessage;
import kr.co.lean.mclient.message.DefaultMessage;
import kr.co.lean.mclient.message.InboxAckMessage;
import kr.co.lean.mclient.message.InboxMessage;
import kr.co.lean.mclient.message.LoginMessage;
import android.util.Log;

public class MClientManagerWorker {

	public static final String TAG = MClientManagerWorker.class.getSimpleName();

	private boolean mEnabled = false;
	private boolean mConnecting = false;

	private MClientManager mMClientManager;
	private SocketInterface mSocketInterface;
	private SendThread mSendThread;
	private ReceiveThread mReceiveThread;

	private ArrayList<DefaultMessage> mSendingList;
	private HashMap<String, Timer> mTimeoutTimerMap;

	private int mRemainMessageCount = 0;

	public String cv = "";
	public String atype = "";
	public String id = "";
	public String pw = "";

	public MClientManagerWorker(MClientManager mClientManager) {
		mMClientManager = mClientManager;
		mSendingList = new ArrayList<DefaultMessage>();
		mTimeoutTimerMap = new HashMap<String, Timer>();
	}

	public void setup(String deviceinfo, String uid, String cv, String atype, String id, String pw) {
		DefaultMessage.deviceinfo = deviceinfo;
		DefaultMessage.uid = uid;
		this.cv = cv;
		this.atype = atype;
		this.id = id;
		this.pw = pw;
	}

	// ////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////// Connect /////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////

	public synchronized void connect() {
		if (mConnecting)
			return;
		if (!canConnect())
			return;
		mConnecting = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mSocketInterface = new SocketInterface("dev.lean.co.kr", 20443, mMClientManager);
					mSendThread = new SendThread(mSocketInterface, mMClientManager);
					mReceiveThread = new ReceiveThread(mSocketInterface, mMClientManager);
					mSendThread.start();
					mReceiveThread.start();
					enable();
					login();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					mConnecting = false;
				}
			}
		}).start();
	}

	private boolean canConnect() {
		if (NetworkBroadcastReceiver.getNetworkState() > 0) {
			return true;
		} else {
			return false;
		}
	}

	public void disconnect() {
		if (mEnabled) {
			mSocketInterface.close();
			mSendThread.interrupt();
			mReceiveThread.interrupt();
			disable();
		}
	}

	public void reconnecct() {
		Log.d(TAG, "reconnect");
		disconnect();
		connect();
	}

	private void disable() {
		mEnabled = false;
	}

	private void enable() {
		mEnabled = true;
	}

	// ////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////// Send
	// ///////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////

	private void addMessageToSendingList(DefaultMessage message) {
		mSendingList.add(message);
	}

	private void removeMessageFromSendList(String messageId) {
		for (int i = 0; i < mSendingList.size(); i++) {
			if (mSendingList.get(i).equals(messageId)) {
				mSendingList.remove(i);
				break;
			}
		}
	}

	private void addMessageToSendThread(DefaultMessage message, boolean resend) {
		if (resend) {
			startTimeoutTimer(message);
		}
		mSendThread.send(message);
	}

	private void checkAndaddMessageToSendThread(DefaultMessage message, boolean resend) {
		if (resend) {
			addMessageToSendingList(message);
		}
		if (mEnabled) {
			addMessageToSendThread(message, resend);
		} else {
			if (!mConnecting) {
				connect();
			}
		}
	}

	private void resendSendingMessage() {
		for (DefaultMessage sendMessage : mSendingList) {
			addMessageToSendThread(sendMessage, true);
		}
	}

	private void login() {
		addMessageToSendThread(new LoginMessage(cv, atype, id, pw), true);
	}

	public void sendMessage(DefaultMessage message, boolean resend) {
		checkAndaddMessageToSendThread(message, resend);
	}

	// ////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////// Receive
	// /////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////

	public void onReceiveAckMessage(AckMessage message) {
		removeMessageFromSendList(message.getMessageId());
		cancelTimeoutTimer(message.getMessageId());
		processAckMessage(message);
	}

	private void processAckMessage(AckMessage message) {
		switch (message.getHeader()) {
		case 'S':
			switch (message.getSubHeader()) {
			case 'L':
				onReceiveLoginAckMessage();
				break;
			}
		}
	}

	private void onReceiveLoginAckMessage() {
		resendSendingMessage();
	}

	public void onReceiveInboxMessage(InboxMessage message) {
		mRemainMessageCount = message.getCount();
	}

	public synchronized void onReceiveMessage() {
		mRemainMessageCount--;
		if (mRemainMessageCount == 0) {
			sendMessage(new InboxAckMessage(), false);
		}
	}

	// ////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////// Timeout /////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////

	private void startTimeoutTimer(final DefaultMessage message) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Log.d(TAG, "timeout " + message.toString());
				mMClientManager.onTimeout(message);
			}
		}, getTimeout());
		mTimeoutTimerMap.put(message.getMessageId(), timer);
	}

	private void cancelTimeoutTimer(String messageId) {
		Timer timer = mTimeoutTimerMap.remove(messageId);
		if (timer != null) {
			try {
				timer.cancel();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private int getTimeout() {
		return 10000;
	}

	public interface OnTimeoutListener {
		void onTimeout(DefaultMessage message);
	}
}