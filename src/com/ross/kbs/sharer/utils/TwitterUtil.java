package com.ross.kbs.sharer.utils;

import java.io.File;

import twitter4j.GeoLocation;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;

import com.ross.kbs.sharer.AccountActivity;
import com.ross.kbs.sharer.R;
import com.ross.kbs.sharer.models.Account;

public class TwitterUtil {

	private static RequestToken requestToken;

	public static Twitter getTwitterInstance(Context context) {
		String consumerKey = context.getString(R.string.twitter_consumer_key);
		String consumerSecret = context
				.getString(R.string.twitter_consumer_secret);

		TwitterFactory factory = new TwitterFactory();
		Twitter twitter = factory.getInstance();
		twitter.setOAuthConsumer(consumerKey, consumerSecret);

		if (hasAccessToken()) {
			twitter.setOAuthAccessToken(loadAccessToken());
		}
		return twitter;
	}

	public static void storeAccessToken(AccessToken accessToken) {
		SharedPreferenceUtil.put(SharedPreferenceUtil.PREF_TWITTER_TOKEN,
				accessToken.getToken());
		SharedPreferenceUtil.put(
				SharedPreferenceUtil.PREF_TWITTER_TOKEN_SECRET,
				accessToken.getTokenSecret());
	}

	public static AccessToken loadAccessToken() {
		String token = SharedPreferenceUtil.getValue(
				SharedPreferenceUtil.PREF_TWITTER_TOKEN, null);
		String tokenSecret = SharedPreferenceUtil.getValue(
				SharedPreferenceUtil.PREF_TWITTER_TOKEN_SECRET, null);
		if (token != null && tokenSecret != null) {
			return new AccessToken(token, tokenSecret);
		} else {
			return null;
		}
	}

	public static boolean hasAccessToken() {
		return loadAccessToken() != null;
	}

	public static void openSession(final Context context) {
		final Twitter twitter = TwitterUtil.getTwitterInstance(context);
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				// TODO Auto-generated method stub
				try {
					requestToken = twitter.getOAuthRequestToken(context
							.getString(R.string.twitter_callbock_url));
					return requestToken.getAuthorizationURL();
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(String url) {
				// TODO Auto-generated method stub
				if (url != null) {
					Intent intent = new Intent(Intent.ACTION_VIEW,
							Uri.parse(url));
					context.startActivity(intent);
				} else {
					// fail
					AlertDialog.Builder alert_confirm = new AlertDialog.Builder(
							context);
					alert_confirm
							.setMessage(
									context.getString(R.string.twitter_connect_error))
							.setCancelable(false)
							.setNegativeButton(
									context.getString(R.string.error_dialog_button_text),
									null);
					AlertDialog alert = alert_confirm.create();
					alert.show();
				}
			}
		};
		task.execute();
	}

	public static void getAccessToken(final Context context, String verifier) {
		final Twitter twitter = TwitterUtil.getTwitterInstance(context);
		AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>() {

			@Override
			protected AccessToken doInBackground(String... params) {
				// TODO Auto-generated method stub
				try {
					return twitter.getOAuthAccessToken(requestToken, params[0]);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(AccessToken result) {
				// TODO Auto-generated method stub
				if (result != null) {
					successOAuth(result);
				} else {
					AlertDialog.Builder alert_confirm = new AlertDialog.Builder(
							context);
					alert_confirm
							.setMessage(
									context.getString(R.string.twitter_connect_error))
							.setCancelable(false)
							.setNegativeButton(
									context.getString(R.string.error_dialog_button_text),
									null);
					AlertDialog alert = alert_confirm.create();
					alert.show();
				}
			}

		};
		task.execute(verifier);
	}

	private static void successOAuth(AccessToken accessToken) {
		TwitterUtil.storeAccessToken(accessToken);
		if (AccountActivity.accountImages != null
				&& AccountActivity.accountListViewAdapter != null) {
			AccountActivity.accountImages[1] = R.drawable.twitter_blue;
			AccountActivity.accountListViewAdapter.getItem(1).setItemImageId(
					R.drawable.twitter_blue);
			AccountActivity.accountListViewAdapter.notifyDataSetChanged();
		}
	}

	public static void closeSession() {
		SharedPreferenceUtil
				.removeValue(SharedPreferenceUtil.PREF_TWITTER_TOKEN);
		SharedPreferenceUtil
				.removeValue(SharedPreferenceUtil.PREF_TWITTER_TOKEN_SECRET);
		if (AccountActivity.accountImages != null
				&& AccountActivity.accountListViewAdapter != null) {
			AccountActivity.accountImages[1] = R.drawable.twitter_white;
			AccountActivity.accountListViewAdapter.getItem(1).setItemImageId(
					R.drawable.twitter_white);
			AccountActivity.accountListViewAdapter.notifyDataSetChanged();
		}
	}

	public static void postMessage(final Activity activity, String message,
			final Location location, final PostCompleteRunnable completeTask) {
		final Twitter twitter = TwitterUtil.getTwitterInstance(activity);
		AsyncTask<String, Void, twitter4j.Status> task = new AsyncTask<String, Void, twitter4j.Status>() {

			@Override
			protected twitter4j.Status doInBackground(String... params) {
				// TODO Auto-generated method stub
				try {
					StatusUpdate update = new StatusUpdate(params[0]);
					if (location != null) {
						update.setLocation(new GeoLocation(location
								.getLatitude(), location.getLongitude()));
					}
					return twitter.updateStatus(update);
				} catch (TwitterException e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onPostExecute(twitter4j.Status result) {
				// TODO Auto-generated method stub
				if (result == null) {
					// fail
					completeTask.init(
							activity.getString(R.string.fail_post_twitter),
							true, false, Account.Twitter.getIndex());
				} else {
					completeTask.init(
							activity.getString(R.string.success_post_twitter),
							true, true, Account.Twitter.getIndex());
				}
				activity.runOnUiThread(completeTask);
			}
		};
		task.execute(message);
	}

	public static void postPhoto(final Activity activity, String message,
			final File imageFile, final Location location,
			final PostCompleteRunnable completeTask) {
		final Twitter twitter = TwitterUtil.getTwitterInstance(activity);
		AsyncTask<String, Void, twitter4j.Status> task = new AsyncTask<String, Void, twitter4j.Status>() {

			@Override
			protected twitter4j.Status doInBackground(String... params) {
				// TODO Auto-generated method stub
				StatusUpdate update = new StatusUpdate(params[0]);
				update.setMedia(imageFile);
				if (location != null) {
					update.setLocation(new GeoLocation(location.getLatitude(),
							location.getLongitude()));
				}
				try {
					return twitter.updateStatus(update);
				} catch (TwitterException e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onPostExecute(twitter4j.Status result) {
				// TODO Auto-generated method stub
				if (result == null) {
					// fail
					completeTask.init(activity
							.getString(R.string.fail_image_post_twitter), true,
							false, Account.Twitter.getIndex());
				} else {
					completeTask.init(activity
							.getString(R.string.success_image_post_twitter),
							true, true, Account.Twitter.getIndex());
				}
				activity.runOnUiThread(completeTask);
			}
		};

		task.execute(message);
	}
}
