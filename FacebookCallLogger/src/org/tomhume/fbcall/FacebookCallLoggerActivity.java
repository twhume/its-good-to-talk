package org.tomhume.fbcall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class FacebookCallLoggerActivity extends Activity implements OnClickListener {

	private static final String TAG = "FacebookCallLoggerActivity";
	public static final String PREFS_NAME = "fb_prefs";
	private Button testButton  = null;
	private Button logoutButton = null;
	private ToggleButton toggler = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		toggler = (ToggleButton) findViewById(R.id.toggleButton1);
		SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		toggler.setChecked(prefs.getBoolean("active", false));
		toggler.setOnClickListener(this);
		
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
		} else if (v==toggler) {
			toggler.setSelected(!toggler.isSelected());
			setActive(toggler.isSelected());
		}
	}

	/**
	 * Turns the logging of phone calls on or off
	 * 
	 * @param state
	 */
	
	private void setActive(boolean state) {
		Log.d(TAG, "setting active flag to " + state);
		SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		Editor ed = prefs.edit();
		ed.putBoolean("active", state);
		ed.commit();
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