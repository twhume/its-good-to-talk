package org.tomhume.fbcall;

import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

public class PhoneCallReporter extends BroadcastReceiver {

	private static final String TAG = "PhoneCallReporter";
	
    @Override
    public void onReceive(Context con, Intent intent) {
    	
        Bundle bundle = intent.getExtras();
        Set<String> keys = bundle.keySet();
        for (String k: keys) {
        	Log.d(TAG, k + "=>" + bundle.getString(k));
        }
        
        /* If a call has just ended, get the number of it */
        
            String duration="0";

              String[] strFields = {
            		  android.provider.CallLog.Calls.NUMBER,
            		  android.provider.CallLog.Calls.TYPE,
            		  android.provider.CallLog.Calls.CACHED_NAME,
            		  android.provider.CallLog.Calls.CACHED_NUMBER_TYPE,
            		  android.provider.CallLog.Calls.DURATION
              };

              String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
              Cursor mCallCursor = con.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI,
            		  strFields,
            		  android.provider.CallLog.Calls.TYPE+"="+android.provider.CallLog.Calls.TYPE,
            		  null,
            		  strOrder);
              mCallCursor.moveToFirst();
              String num = android.provider.CallLog.Calls.getLastOutgoingCall(con);
              Log.d(TAG, "getLastOutgoingCall="+num);
              num =   mCallCursor.getString(mCallCursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
              Log.d(TAG, "direct view="+num);
                     	
          
    }

}