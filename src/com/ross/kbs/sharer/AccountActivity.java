package com.ross.kbs.sharer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.facebook.Session;
import com.ross.kbs.sharer.models.Account;
import com.ross.kbs.sharer.models.AccountItem;
import com.ross.kbs.sharer.models.AccountListViewAdapter;
import com.ross.kbs.sharer.utils.FacebookUtil;
import com.ross.kbs.sharer.utils.KakaoUtil;
import com.ross.kbs.sharer.utils.SharedPreferenceUtil;
import com.ross.kbs.sharer.utils.TwitterUtil;

public class AccountActivity extends Activity implements OnItemClickListener {

	ListView accountListView;
	List<AccountItem> rowItems;

	public static String[] accountTitles;
	public static Integer[] accountImages;
	public static AccountListViewAdapter accountListViewAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account);
		SharedPreferenceUtil.setGlobalContext(getApplicationContext());

		accountTitles = new String[3];
		accountImages = new Integer[3];
		for (Account account : Account.values()) {
			accountTitles[account.getIndex()] = account.getName();
			accountImages[account.getIndex()] = account.getWhiteImageId();
		}

		FacebookUtil.restoreSession(this, savedInstanceState);
		initListView();
	}

	private void initListView() {
		Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			accountImages[Account.Facebook.getIndex()] = Account.Facebook
					.getBlueImageId();
		}
		if (TwitterUtil.hasAccessToken()) {
			accountImages[Account.Twitter.getIndex()] = Account.Twitter
					.getBlueImageId();
		}

		com.kakao.Session.initializeSession(this, KakaoUtil.sessionCallback);
		if (com.kakao.Session.getCurrentSession().isOpened()) {
			accountImages[Account.KakaoStory.getIndex()] = Account.KakaoStory
					.getBlueImageId();
		}

		rowItems = new ArrayList<AccountItem>();
		for (int i = 0; i < accountTitles.length; i++) {
			AccountItem item = new AccountItem(accountImages[i],
					accountTitles[i]);
			rowItems.add(item);
		}

		accountListView = (ListView) findViewById(R.id.accountListView);
		accountListViewAdapter = new AccountListViewAdapter(this,
				R.layout.account_list_view_item, rowItems);
		accountListView.setAdapter(accountListViewAdapter);
		accountListView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if (arg2 == Account.Facebook.getIndex()) {
			// Facebook
			if (FacebookUtil.isOpen()) {
				showLogoutAlert(new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						FacebookUtil.closeSession();
					}
				});
			} else {
				FacebookUtil.openSession(this);
			}
		} else if (arg2 == Account.Twitter.getIndex()) {
			// Twitter
			if (TwitterUtil.hasAccessToken()) {
				showLogoutAlert(new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						TwitterUtil.closeSession();
					}
				});
			} else {
				TwitterUtil.openSession(this);
			}
		} else if (arg2 == Account.KakaoStory.getIndex()) {
			// KakaoStory
			com.kakao.Session session = com.kakao.Session.getCurrentSession();
			if (session != null && session.isOpened()) {
				showLogoutAlert(new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						KakaoUtil.closeSession();
					}
				});
			} else {
				KakaoUtil.openSession(this);
			}
		}
	}

	private void showLogoutAlert(DialogInterface.OnClickListener listener) {
		AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
		alert_confirm.setMessage(getString(R.string.signout_question_text))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.yes_text), listener)
				.setNegativeButton(getString(R.string.no_text), null);
		AlertDialog alert = alert_confirm.create();
		alert.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		com.facebook.Session session = com.facebook.Session.getActiveSession();
		if (session != null) {
			session.onActivityResult(this, requestCode, resultCode, data);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		com.facebook.Session session = com.facebook.Session.getActiveSession();
		com.facebook.Session.saveSession(session, outState);
	}

	@Override
	public void onStart() {
		super.onStart();
		com.facebook.Session session = com.facebook.Session.getActiveSession();
		if (session != null) {
			session.addCallback(FacebookUtil.statusCallback);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		com.facebook.Session session = com.facebook.Session.getActiveSession();
		if (session != null) {
			session.addCallback(FacebookUtil.statusCallback);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		com.kakao.Session.initializeSession(this, KakaoUtil.sessionCallback);
	}

	public void goBack(View view) {
		finish();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		if (intent == null
				|| intent.getData() == null
				|| !intent.getData().toString()
						.startsWith(getString(R.string.twitter_callbock_url))) {
			return;
		}
		String verifier = intent.getData().getQueryParameter("oauth_verifier");
		TwitterUtil.getAccessToken(this, verifier);
	}
}
