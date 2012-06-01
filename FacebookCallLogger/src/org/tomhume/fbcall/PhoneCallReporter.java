package org.tomhume.fbcall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PhoneCallReporter extends BroadcastReceiver {

	private static final String TAG = "PhoneCallReporter";

	@Override
	public void onReceive(Context con, Intent intent) {
		Log.d(TAG, "onReceive()");

		/* If a call has just ended, get the number of it */

		
		String num = android.provider.CallLog.Calls.getLastOutgoingCall(con);
		Log.d(TAG, "getLastOutgoingCall=" + num);
		Intent i = new Intent(con, LoggingService.class);
		i.putExtra("msisdn", num);
		con.startService(i);
	}

}