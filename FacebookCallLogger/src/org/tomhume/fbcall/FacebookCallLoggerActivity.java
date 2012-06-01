package org.tomhume.fbcall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class FacebookCallLoggerActivity extends Activity implements OnClickListener {

	private static final String TAG = "FacebookCallLoggerActivity";
	private Button testButton  = null;
	private Button logoutButton = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		testButton = (Button) findViewById(R.id.test_button);
		testButton.setOnClickListener(this);

		logoutButton = (Button) findViewById(R.id.logout_button);
		logoutButton.setOnClickListener(this);

		forceAuthorize();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		FacebookCallLogger.facebook.authorizeCallback(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		Log.d(TAG, "CLICK! " + v);
		if (v==testButton) {
			FacebookCallLogger l = new FacebookCallLogger(getApplicationContext(), "07929169110");
			l.go();
		} else if (v==logoutButton) {
			forceAuthorize();
		}
	}


	
	private void forceAuthorize() {
		FacebookCallLogger.facebook.authorize(this, FacebookCallLogger.permissions, Facebook.FORCE_DIALOG_AUTH, new DialogListener() {
			@Override
			public void onComplete(Bundle values) {
			}

			@Override
			public void onFacebookError(FacebookError error) {
			}

			@Override
			public void onError(DialogError e) {
			}

			@Override
			public void onCancel() {
			}
		});
	}
	






}