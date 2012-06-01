package org.tomhume.fbcall;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.facebook.android.*;
import com.facebook.android.Facebook.*;


public class FacebookCallLoggerActivity extends Activity implements OnClickListener {

	private static final String TAG = "FacebookCallLoggerActivity";
    private Facebook facebook = new Facebook("429957320362642");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button button = (Button)findViewById(R.id.test_button);
        button.setOnClickListener(this);

        facebook.authorize(this, new DialogListener() {
            @Override
            public void onComplete(Bundle values) {}

            @Override
            public void onFacebookError(FacebookError error) {}

            @Override
            public void onError(DialogError e) {}

            @Override
            public void onCancel() {}
        });
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebook.authorizeCallback(requestCode, resultCode, data);
    }

	@Override
	public void onClick(View v) {
		Log.d(TAG, "CLICK!");
		String name = getContactNameFromNumber("07929169110");
//		String profileId = getProfileIdFromFacebook();
		Log.d(TAG, "name="+name);
	}
	
	public String getContactNameFromNumber(String number) {
	    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	    String name = "?";

	    ContentResolver contentResolver = getContentResolver();
	    Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
	            ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

	    try {
	        if (contactLookup != null && contactLookup.getCount() > 0) {
	        		contactLookup.moveToNext();
	        		name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
	        	}
	            Log.d(TAG, contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID)));
	    } finally {
	        if (contactLookup != null) {
	            contactLookup.close();
	        }
	    }

	    return name;
	}
    
}