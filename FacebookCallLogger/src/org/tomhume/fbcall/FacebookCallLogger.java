package org.tomhume.fbcall;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.Facebook;
import com.facebook.android.Util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

public class FacebookCallLogger {
	
	private static final String TAG = "FacebookCallLogger";
	public static final String[] permissions = new String[] {"publish_stream", "publish_actions"};
	public static Facebook facebook = new Facebook("429957320362642");

	private Context ctx = null;
	private String number = null;
	
	public FacebookCallLogger(Context c, String n) {
		this.ctx = c;
		this.number = n;
	}
	
	public void go() {
		String[] names = getContactNameFromNumber(number);
		if (names == null) Log.d(TAG, "no names");
		else Log.d(TAG, "names=" + joinStrings(names));
		new GetFriendsTask().execute(names);
	}
	
	class GetFriendsTask extends AsyncTask<String[], Void, List<DisplayableFriend>> {

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
					String fql = "SELECT uid,name,profile_url,pic_square FROM user WHERE name = '" + name[j] + "' AND uid IN (SELECT uid2 FROM friend WHERE uid1 = me())";
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
						d.id = friend.getString("uid");
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
	    		new PostCallTask().execute(result.get(0));
	    	} else {
	    		Log.e(TAG, "Missing feature: need dialog to query which user");
	    	}
		}
	 }
	
	class PostCallTask extends AsyncTask<DisplayableFriend, Void, Void> {

		@Override
		protected Void doInBackground(DisplayableFriend... params) {
			Log.d(TAG, "PostCallTask()");
			Bundle b = new Bundle();
			b.putString("profile", params[0].profile);
			b.putString("tags", params[0].id);
			try {
				String response = facebook.request("me/twh_call_logger:call", b, "POST");
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

		ContentResolver contentResolver = ctx.getContentResolver();
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
