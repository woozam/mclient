package kr.co.lean.mclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.lean.mclient.message.AckMessage;
import kr.co.lean.mclient.message.DefaultMessage;
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

	public MClientManagerWorker(MClientManager mClientManager) {
		mMClientManager = mClientManager;
		mSendingList = new ArrayList<DefaultMessage>();
		mTimeoutTimerMap = new HashMap<String, Timer>();
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Connect /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////

	public synchronized void connect() {
		if (mConnecting)
			return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mConnecting = true;
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

	public void disconnect() {
		mSocketInterface.close();
		mSendThread.interrupt();
		mReceiveThread.interrupt();
		disable();
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
	
	
	//////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Send ///////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////

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

	private void addMessageToSendThread(DefaultMessage message) {
		startTimeoutTimer(message);
		mSendThread.send(message);
	}

	private void checkAndaddMessageToSendThread(DefaultMessage message) {
		addMessageToSendingList(message);
		if (mEnabled) {
			addMessageToSendThread(message);
		} else {
			if (!mConnecting) {
				connect();
			}
		}
	}

	private void resendSendingMessage() {
		for (DefaultMessage sendMessage : mSendingList) {
			addMessageToSendThread(sendMessage);
		}
	}

	private void login() {
		addMessageToSendThread(new LoginMessage());
	}

	public void sendMessage(DefaultMessage message) {
		checkAndaddMessageToSendThread(message);
	}

	
	//////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Receive /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	
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
		
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Timeout /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////

	private void startTimeoutTimer(final DefaultMessage message) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Log.d(TAG, "timeout " + message.toString());
				mMClientManager.onTimeout(message);
			}
		}, 10000);
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

	public interface OnTimeoutListener {
		void onTimeout(DefaultMessage message);
	}
}