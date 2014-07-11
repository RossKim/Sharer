package com.ross.kbs.sharer.utils;

import com.ross.kbs.sharer.PublishActivity;

public class PostCompleteRunnable implements Runnable {

	private PublishActivity activity;
	String message;
	boolean result;
	boolean success;
	int index;

	public PostCompleteRunnable(PublishActivity activity) {
		this.activity = activity;
	}

	public PostCompleteRunnable init(String message, boolean result,
			boolean success, int index) {
		// TODO Auto-generated constructor stub
		this.message = message;
		this.result = result;
		this.success = success;
		this.index = index;
		return this;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		activity.changePostStatus(message, result, success, index);
	}
}
