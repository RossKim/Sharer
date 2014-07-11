package com.ross.kbs.sharer.utils;

import java.io.File;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.kakao.APIErrorResult;
import com.kakao.KakaoParameterException;
import com.kakao.KakaoStoryHttpResponseHandler;
import com.kakao.KakaoStoryService;
import com.kakao.KakaoStoryService.StoryType;
import com.kakao.LogoutResponseCallback;
import com.kakao.MyStoryInfo;
import com.kakao.NoteKakaoStoryPostParamBuilder;
import com.kakao.PhotoKakaoStoryPostParamBuilder;
import com.kakao.Session;
import com.kakao.SessionCallback;
import com.kakao.UserManagement;
import com.kakao.authorization.authcode.AuthType;
import com.kakao.exception.KakaoException;
import com.kakao.helper.TalkProtocol;
import com.ross.kbs.sharer.AccountActivity;
import com.ross.kbs.sharer.R;
import com.ross.kbs.sharer.models.Account;

public class KakaoUtil {

	public static final SessionCallback sessionCallback = new MySessionStatusCallback();
	private static PostCompleteRunnable completeTask;
	private static Activity runningActivity;

	public static void openSession(Context context) {
		if (TalkProtocol.existCapriLoginActivityInTalk(context)) {
			showLoginOption(context);
		} else {
			Session.getCurrentSession().open(sessionCallback,
					AuthType.KAKAO_ACCOUNT);
		}
	}

	private static void showLoginOption(final Context context) {
		final Item[] items = {
				new Item(R.string.com_kakao_kakaotalk_account,
						R.drawable.kakaotalk_icon),
				new Item(R.string.com_kakao_other_kakaoaccount,
						R.drawable.kakaoaccount_icon),
				new Item(R.string.com_kakao_account_cancel, 0),// no icon for
																// this one
		};

		final ListAdapter adapter = new ArrayAdapter<Item>(context,
				android.R.layout.select_dialog_item, android.R.id.text1, items) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				TextView tv = (TextView) v.findViewById(android.R.id.text1);

				tv.setText(items[position].textId);
				tv.setTextSize(15);
				tv.setGravity(Gravity.CENTER);
				if (position == 2) {
					tv.setBackgroundResource(R.drawable.kakao_cancel_button_background);
				} else {
					tv.setBackgroundResource(R.drawable.kakao_account_button_background);
				}
				tv.setCompoundDrawablesWithIntrinsicBounds(
						items[position].icon, 0, 0, 0);

				int dp5 = (int) (5 * context.getResources().getDisplayMetrics().density + 0.5f);
				tv.setCompoundDrawablePadding(dp5);

				return v;
			}
		};

		new AlertDialog.Builder(context)
				.setAdapter(adapter, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int item) {
						if (item == 0) {// 카톡으로 시작
							Session.getCurrentSession().open(sessionCallback);
						} else if (item == 1) {// 계정 직접 입력
							Session.getCurrentSession().open(sessionCallback,
									AuthType.KAKAO_ACCOUNT);
						} else if (item == 2) {// 취소
							dialog.dismiss();
						}
					}
				}).create().show();

	}

	private static class Item {
		public final int textId;
		public final int icon;

		public Item(int textId, Integer icon) {
			this.textId = textId;
			this.icon = icon;
		}
	}

	public static void closeSession() {
		UserManagement.requestLogout(new LogoutResponseCallback() {

			@Override
			protected void onSuccess(long userId) {
				// TODO Auto-generated method stub
				if (AccountActivity.accountImages != null
						&& AccountActivity.accountListViewAdapter != null) {
					AccountActivity.accountImages[2] = R.drawable.blanklogo;
					AccountActivity.accountListViewAdapter.getItem(2)
							.setItemImageId(R.drawable.blanklogo);
					AccountActivity.accountListViewAdapter
							.notifyDataSetChanged();
				}
			}

			@Override
			protected void onFailure(APIErrorResult errorResult) {
				// TODO Auto-generated method stub
				if (AccountActivity.accountImages != null
						&& AccountActivity.accountListViewAdapter != null) {
					AccountActivity.accountImages[2] = R.drawable.blanklogo;
					AccountActivity.accountListViewAdapter.getItem(2)
							.setItemImageId(R.drawable.blanklogo);
					AccountActivity.accountListViewAdapter
							.notifyDataSetChanged();
				}
			}
		});
	}

	private static void completePosting() {
		if (completeTask != null && runningActivity != null) {
			runningActivity.runOnUiThread(completeTask);
		}
		completeTask = null;
		runningActivity = null;
	}

	public static void postMessage(final Activity activity,
			final String message, final PostCompleteRunnable completeTask) {
		runningActivity = activity;
		KakaoUtil.completeTask = completeTask;
		final NoteKakaoStoryPostParamBuilder postParamBuilder = new NoteKakaoStoryPostParamBuilder(
				message);
		try {
			final Bundle parameters = postParamBuilder.build();
			KakaoStoryService.requestPost(StoryType.NOTE,
					new MyStoryHttpResponseHandler<MyStoryInfo>() {

						@Override
						protected void onHttpSuccess(MyStoryInfo myStoryInfo) {
							// TODO Auto-generated method stub
							completeTask.init(
									activity.getString(R.string.success_post_kakaostory),
									true, true, Account.KakaoStory.getIndex());
							completePosting();
						}
					}, parameters);
		} catch (KakaoParameterException e) {
			// 파라미터에 문제 발생
			completeTask.init(
					activity.getString(R.string.fail_post_kakaostory), true,
					false, Account.KakaoStory.getIndex());
			completePosting();
		}
	}

	public static void postPhoto(final Activity activity, final String message,
			final File imageFile, final PostCompleteRunnable completeTask) {
		runningActivity = activity;
		KakaoUtil.completeTask = completeTask;
		try {
			KakaoStoryService.requestMultiUpload(
					new MyStoryHttpResponseHandler<String[]>() {
						@Override
						protected void onHttpSuccess(final String[] imageURLs) {
							if (imageURLs != null && imageURLs.length != 0) {
								// 성공
								// 사진 포스팅 계속
								requestPostPhoto(activity, imageURLs, message,
										completeTask);
							} else {
								// 실패
								completePosting();
							}
						}
					}, Arrays.asList(imageFile));
		} catch (Exception e) {
			// 이미지 업로드 실패
			completeTask.init(
					activity.getString(R.string.fail_image_post_kakaostory),
					true, false, Account.KakaoStory.getIndex());
			completePosting();
		}
	}

	private static void requestPostPhoto(final Activity activity,
			String[] imageURLs, String message,
			final PostCompleteRunnable completeTask) {
		final PhotoKakaoStoryPostParamBuilder postParamBuilder = new PhotoKakaoStoryPostParamBuilder(
				imageURLs);
		if (message != null) {
			postParamBuilder.setContent(message);
		}
		try {
			final Bundle parameters = postParamBuilder.build();
			KakaoStoryService.requestPost(StoryType.PHOTO,
					new MyStoryHttpResponseHandler<MyStoryInfo>() {
						@Override
						protected void onHttpSuccess(
								final MyStoryInfo myStoryInfo) {
							completeTask.init(
									activity.getString(R.string.success_image_post_kakaostory),
									true, true, Account.KakaoStory.getIndex());
							completePosting();
						}
					}, parameters);
		} catch (KakaoParameterException e) {
			// 파라미터에 문제 발생
			completeTask.init(
					activity.getString(R.string.fail_image_post_kakaostory),
					true, false, Account.KakaoStory.getIndex());
			completePosting();
		}
	}

	private static class MySessionStatusCallback implements SessionCallback {
		@Override
		public void onSessionOpened() {
			if (AccountActivity.accountImages != null
					&& AccountActivity.accountListViewAdapter != null) {
				AccountActivity.accountImages[2] = R.drawable.kakaostory;
				AccountActivity.accountListViewAdapter.getItem(2)
						.setItemImageId(R.drawable.kakaostory);
				AccountActivity.accountListViewAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onSessionClosed(KakaoException exception) {
			// TODO Auto-generated method stub
			if (AccountActivity.accountImages != null
					&& AccountActivity.accountListViewAdapter != null) {
				AccountActivity.accountImages[2] = R.drawable.blanklogo;
				AccountActivity.accountListViewAdapter.getItem(2)
						.setItemImageId(R.drawable.blanklogo);
				AccountActivity.accountListViewAdapter.notifyDataSetChanged();
			}
		}

	}

	private static abstract class MyStoryHttpResponseHandler<T> extends
			KakaoStoryHttpResponseHandler<T> {
		@Override
		protected void onHttpSessionClosedFailure(
				final APIErrorResult errorResult) {
			if (completeTask != null && runningActivity != null) {
				completeTask.init(runningActivity
						.getString(R.string.fail_post_kakaostory), true, false,
						Account.KakaoStory.getIndex());
			}
			completePosting();
		}

		@Override
		protected void onNotKakaoStoryUser() {
			if (completeTask != null && runningActivity != null) {
				completeTask.init(runningActivity
						.getString(R.string.fail_post_kakaostory), true, false,
						Account.KakaoStory.getIndex());
			}
			completePosting();
		}

		@Override
		protected void onFailure(final APIErrorResult errorResult) {
			if (completeTask != null && runningActivity != null) {
				completeTask.init(runningActivity
						.getString(R.string.fail_post_kakaostory), true, false,
						Account.KakaoStory.getIndex());
			}
			completePosting();
		}
	}
}
