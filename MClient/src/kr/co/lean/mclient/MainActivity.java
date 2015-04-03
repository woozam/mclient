package kr.co.lean.mclient;

import kr.co.lean.mclient.MClientManager.OnLogListener;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener, OnLogListener {
	
	private Button mPing;
	private Button mClear;
	private ScrollView mScrollView;
	private TextView mLog;

	public String deviceinfo = "A0";
	public String uid = "JP000000000087";
	public String cv = "A000000";
	public String atype = "FB";
	public String id = "821554864586701";
	public String pw = "170776ff52b2a4bf1fea58e8abb203643e7e367a82b57cb7d4ee891e95e237577052f144d3602bb78171c1359be58f8b42380ed9f86a432a0aed90cfe3a99157";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mPing = (Button) findViewById(R.id.button1);
		mPing.setOnClickListener(this);
		mClear = (Button) findViewById(R.id.clear);
		mClear.setOnClickListener(this);
		mScrollView = (ScrollView) findViewById(R.id.scroll_view);
		mLog = (TextView) findViewById(R.id.log);
		MClientManager.getInstance().setup(deviceinfo, uid, cv, atype, id, pw);
		MClientManager.getInstance().setOnLogListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == mPing) {
			MClientManager.getInstance().keepAlive();
		} else if (v == mClear) {
			mLog.setText(null);
		}
	}

	@Override
	public void onLog(final String log) {
		mLog.post(new Runnable() {
			@Override
			public void run() {
				mLog.setText(mLog.getText() + "\n" + log);
				mScrollView.post(new Runnable() {
					@Override
					public void run() {
						mScrollView.smoothScrollBy(0, 1000);
					}
				});
			}
		});
	}
}