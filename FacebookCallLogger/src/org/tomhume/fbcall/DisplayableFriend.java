package org.tomhume.fbcall;

/**
 * Helper class to encapsulate a single friend, who can be displayed on-screen in a selection list
 * 
 * @author twhume
 *
 */

public class DisplayableFriend {
	public String id;
	public String name;
	public String picture;
	public String profile;
	
	public String toString() {
		String s = new String("DisplayableFriend(");
		s += id + ",";
		s += name + ",";
		s += picture + ",";
		s += profile + ")";
		return s;
	}
}
