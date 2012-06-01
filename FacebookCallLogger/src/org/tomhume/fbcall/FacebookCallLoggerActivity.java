package org.tomhume.fbcall;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
	private static final String[] permissions = new String[] {"publish_stream", "publish_actions"};
	private Facebook facebook = new Facebook("429957320362642");
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
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		Log.d(TAG, "CLICK! " + v);
		if (v==testButton) {
			String[] names = getContactNameFromNumber("07929169110");
			if (names == null) Log.d(TAG, "no names");
			else Log.d(TAG, "names=" + joinStrings(names));
			new GetFriendsTask().execute(names);
		} else if (v==logoutButton) {
			forceAuthorize();
		}
	}

	class GetFriendsTask extends AsyncTask<String[], Void, List<DisplayableFriend>> {

	    private Exception exception;

	    protected List<DisplayableFriend> doInBackground(String[]... name) {
	    	List<DisplayableFriend> friends = getMatchingFriends(name[0]);
			return friends;
	    }
	    
	    
	    	
	    private List<DisplayableFriend> getMatchingFriends(String[] name) {	
			ArrayList<DisplayableFriend> ret = new ArrayList<DisplayableFriend>();

			try {
				for (int j = 0; j < name.length; j++) {
					Bundle params = new Bundle();
					params.putString("method", "fql.query");
					String fql = "SELECT name,profile_url,pic_square FROM user WHERE name = '" + name[j] + "' AND uid IN (SELECT uid2 FROM friend WHERE uid1 = me())";
					Log.d(TAG, "FQL="+fql);
					params.putString("query", fql);
					String response = facebook.request(params);
					Log.d(TAG, "got response " + response);
					response = "{\"data\":" + response + "}";

					JSONObject json = Util.parseJson(response);
					JSONArray data = json.getJSONArray("data");

					for (int i = 0, size = data.length(); i < size; i++) {
						JSONObject friend = data.getJSONObject(i);
						DisplayableFriend d = new DisplayableFriend();
						d.picture = friend.getString("pic_square");
						d.profile = friend.getString("profile_url");
						d.name = friend.getString("name");
						ret.add(d);
						Log.d(TAG, "added friend " + d);
					}
				}
			} catch (JSONException e) {
				Log.e(TAG, "JSON Parsing error " + e);
			} catch (MalformedURLException e) {
				Log.e(TAG, "MalformedURLException " + e);
			} catch (IOException e) {
				Log.e(TAG, "IOException " + e);
			}
			return deduplicate(ret);
		}

		@Override
		protected void onPostExecute(List<DisplayableFriend> result) {
    		// post to the wall
	    	if (result.size()==1) {
	    		new PostCallTask().execute(result.get(0).profile);
	    	} else {
	    		Log.e(TAG, "Missing feature: need dialog to query which user");
	    	}
		}
	 }
	
	private void forceAuthorize() {
		facebook.authorize(this, permissions, Facebook.FORCE_DIALOG_AUTH, new DialogListener() {
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
	
	class PostCallTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			Log.d(TAG, "PostCallTask()");
			Bundle b = new Bundle();
			b.putString("profile", params[0]);
			try {
				String response = facebook.request("me/twh_call_logger:call", b);
				Log.d(TAG, response);
			} catch (MalformedURLException e) {
				Log.e(TAG, "MalformedURLException " + e);
			} catch (IOException e) {
				Log.e(TAG, "IOException " + e);
			}
			return null;
		}

	}

	
	private List<DisplayableFriend> deduplicate(List<DisplayableFriend> list) {
		Hashtable<String,Boolean> exists = new Hashtable<String, Boolean>();
		for (DisplayableFriend df: list) {
			if (exists.containsKey(df.profile)) list.remove(df);
			exists.put(df.profile, new Boolean(true));
		}
		return list;
	}

	private String joinStrings(String[] in) {
		String ret = "";
		for (int i = 0; i < in.length; i++) {
			ret = ret + in[i] + ",";
		}
		if (ret.length() > 0) ret = ret.substring(0, ret.length() - 1);
		return ret;
	}

	public String[] getContactNameFromNumber(String number) {
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		String[] names = null;

		ContentResolver contentResolver = getContentResolver();
		Cursor contactLookup = contentResolver.query(uri, new String[] { BaseColumns._ID,
				ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

		try {
			if (contactLookup != null && contactLookup.getCount() > 0) {
				names = new String[contactLookup.getCount()];
				for (int i = 0; i < contactLookup.getCount(); i++) {
					contactLookup.moveToNext();
					names[i] = contactLookup
							.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
				}
			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}

		return names;
	}

}