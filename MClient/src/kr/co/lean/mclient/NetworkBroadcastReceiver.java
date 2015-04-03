package kr.co.lean.mclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkBroadcastReceiver extends BroadcastReceiver {

	public static final String TAG = NetworkBroadcastReceiver.class.getSimpleName();
	private static int mLastState = getNetworkState();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
		}

		int state = getNetworkState();

		if (state != mLastState) {
			mLastState = state;
			//TODO
		}
	}

	public static int getNetworkState() {
		Context context = MClientApplication.mContext;
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		int currentActiveNetwork = 0;
		if (wifi != null && wifi.isConnected()) {
			// wifi
			currentActiveNetwork = 1;
		} else if (mobile != null && mobile.isConnected()) {
			// 3g
			currentActiveNetwork = 2;
			if (mobile.getSubtypeName().equals("LTE")) {
				// lte
				currentActiveNetwork = 3;
			}
		} else {
			if (android.os.Build.VERSION.SDK_INT >= 8) {
				NetworkInfo wimax = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
				if (wimax != null && wimax.isConnected()) {
					// wibro
					currentActiveNetwork = 3;
				}
			}
		}
		return currentActiveNetwork;
	}
}