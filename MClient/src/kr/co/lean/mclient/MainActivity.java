package kr.co.lean.mclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
	
	private Button mPing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mPing = (Button) findViewById(R.id.button1);
		mPing.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == mPing) {
			MClientManager.getInstance().keepAlive();
		}
	}
}