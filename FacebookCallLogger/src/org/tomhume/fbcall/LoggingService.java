package org.tomhume.fbcall;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class LoggingService extends IntentService {

		private static final String TAG = "LoggingService";
	
	    public LoggingService(String name) {
	        super(name);
	    }

	    public LoggingService(){
	        super("LoggingService");    
	    }

	    @Override
	    protected void onHandleIntent(Intent intent) {
	    	Log.d(TAG, "onHandleIntent()");
	    	FacebookCallLogger logger = new FacebookCallLogger(getApplicationContext(), intent.getStringExtra("msisdn"));
	    	logger.go();
	    }

	
}
