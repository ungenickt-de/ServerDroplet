package com.playerrealms.droplet.tablist;

public enum TabListSortRank {

	NONE('0'),
	FIRST('a'),
	SECOND('b'),
	THIRD('c'),
	FOURTH('d'),
	FIFTH('e'),
	SIXTH('f'),
	SEVENTH('g'),
	EIGHTH('h'),
	NINTH('i'),
	TENTH('j');
	
	private final char prefix;
	
	private TabListSortRank(char prefix) {
		this.prefix = prefix;
	}
	
	public char getPrefix() {
		return prefix;
	}
	
}
