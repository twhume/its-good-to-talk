package org.tomhume.fbcall;

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
