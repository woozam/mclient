package kr.co.lean.mclient;

import android.app.Application;
import android.content.Context;

public class MClientApplication extends Application {

	public static Context mContext;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
	}
}