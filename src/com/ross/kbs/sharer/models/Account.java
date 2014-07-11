package com.ross.kbs.sharer.models;

import com.ross.kbs.sharer.R;

public enum Account {
	Facebook(0, "Facebook", R.drawable.fb_white, R.drawable.fb_blue), Twitter(
			1, "Twitter", R.drawable.twitter_white, R.drawable.twitter_blue), KakaoStory(
			2, "KakaoStory", R.drawable.blanklogo, R.drawable.kakaostory);

	private int index;
	private String name;
	private int whiteImageId;
	private int blueImageId;

	Account(int index, String name, int whiteImageId, int blueImageId) {
		this.index = index;
		this.name = name;
		this.whiteImageId = whiteImageId;
		this.blueImageId = blueImageId;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public int getWhiteImageId() {
		return whiteImageId;
	}

	public int getBlueImageId() {
		return blueImageId;
	}
}
