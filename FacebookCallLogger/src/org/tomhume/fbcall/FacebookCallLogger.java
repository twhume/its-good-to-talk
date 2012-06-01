package org.tomhume.fbcall;

/**
 * Handles the sending of an Action to Facebook, using the Open Graph API
 */

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

	/**
	 * Kick everything off, if necessary
	 */
	
	public void go() {
		String[] names = getContactNameFromNumber(number);
		if (names == null) Log.d(TAG, "no names");
		else Log.d(TAG, "names=" + joinStrings(names));
		new GetFriendsTask().execute(names);
	}
	
	/**
	 * Background task which takes an array of names, looks them up in Facebook, and returns a list 
	 * of DisplayableFriends with profile picture URLs, IDs, etc.
	 * 
	 * @author twhume
	 *
	 */
	
	class GetFriendsTask extends AsyncTask<String[], Void, List<DisplayableFriend>> {

	    protected List<DisplayableFriend> doInBackground(String[]... name) {
	    	List<DisplayableFriend> friends = getMatchingFriends(name[0]);
			return friends;
	    }
	    
	    /**
	     * Do the look-up of all friends, using FQL
	     * 
	     * @param names array of friends names
	     * @return
	     */
	    
	    private List<DisplayableFriend> getMatchingFriends(String[] names) {	
			ArrayList<DisplayableFriend> ret = new ArrayList<DisplayableFriend>();

			try {
				for (int j = 0; j < names.length; j++) {
					Bundle params = new Bundle();
					params.putString("method", "fql.query");
					String fql = "SELECT uid,name,profile_url,pic_square FROM user WHERE name = '" + names[j] + "' AND uid IN (SELECT uid2 FROM friend WHERE uid1 = me())";
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

	    /**
	     * If we have no names in our list, do nothing;
	     * If we have one, post an update to Facebook about them;
	     * TODO: if we have more than one, pop up a dialog box so the user can choose which
	     */
	    
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

	/**
	 * Post to the Open Graph, to record a call having been made.
	 * 
	 * @author twhume
	 *
	 */
	
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
	
	/**
	 * Takes a List of DisplayableFriends, which may contain duplicates, and returns a deduplicated list.
	 * 
	 * @param list
	 * @return
	 */
	
	private List<DisplayableFriend> deduplicate(List<DisplayableFriend> list) {
		Hashtable<String,Boolean> exists = new Hashtable<String, Boolean>();
		for (DisplayableFriend df: list) {
			if (exists.containsKey(df.profile)) list.remove(df);
			exists.put(df.profile, new Boolean(true));
		}
		return list;
	}
	
	/**
	 * Helper method to join some strings together 
	 * 
	 * @param in
	 * @return
	 */
	
	private String joinStrings(String[] in) {
		String ret = "";
		for (int i = 0; i < in.length; i++) {
			ret = ret + in[i] + ",";
		}
		if (ret.length() > 0) ret = ret.substring(0, ret.length() - 1);
		return ret;
	}
	
	/**
	 * Search the on-phone address book for all contacts who have the phone number provided.
	 * Return an array of their names.
	 * 
	 * @param number
	 * @return
	 */

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
