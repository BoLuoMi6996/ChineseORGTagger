
package edu.zju.udms.model;

public enum Tag {
	Begin,Inter,End,Other;
	public static  boolean adjacent(Tag last,Tag next){
		return last==Begin && next==Inter ||
		last == Inter && next == End || 
		last==Inter && next==Inter ||
		last == End && next == Other ||
		last == Other && next == Begin ||
		last == Other && next == Other ||
		last == Begin && next == End;
	}
	public static Tag index(int i){
		return Tag.values()[i];
	}
}

