package com.ross.kbs.sharer.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.AccessTokenSource;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphObject;
import com.ross.kbs.sharer.AccountActivity;
import com.ross.kbs.sharer.R;
import com.ross.kbs.sharer.models.Account;

public class FacebookUtil {

	private static final List<String> PERMISSIONS = Arrays
			.asList("publish_actions");
	public static Session.StatusCallback statusCallback = new FacebookUtil.SessionStatusCallback();
	@SuppressLint("SimpleDateFormat")
	private static final DateFormat dateFormat = new SimpleDateFormat(
			"EEE MMM dd HH:mm:ss zzz yyyy");

	private static final int REAUTH_ACTIVITY_CODE = 100;

	public static void restoreSession(Activity activity,
			Bundle savedInstanceState) {
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(activity, null,
						statusCallback, savedInstanceState);
			} else {
				String accessTokenString = SharedPreferenceUtil.getValue(
						SharedPreferenceUtil.PREF_FACEBOOK_TOKEN, null);
				String expireDateString = SharedPreferenceUtil.getValue(
						SharedPreferenceUtil.PREF_FACEBOOK_EXPIRE_DATE, null);
				if (accessTokenString != null) {
					try {
						Date expireDate = dateFormat.parse(expireDateString);
						AccessToken accessToken = AccessToken
								.createFromExistingAccessToken(
										accessTokenString,
										expireDate,
										new Date(),
										AccessTokenSource.FACEBOOK_APPLICATION_NATIVE,
										PERMISSIONS);
						Session.openActiveSessionWithAccessToken(activity,
								accessToken, statusCallback);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			if (session != null && session.isOpened()) {
				List<String> permissions = session.getPermissions();
				if (!permissions.containsAll(PERMISSIONS)) {
					requestPublishPermissions(activity, session);
				}
			}
		}
	}

	public static void openSession(final Activity activity) {
		if (com.facebook.Session.getActiveSession() != null) {
			closeSession();
		}
		com.facebook.Session.openActiveSession(activity, true,
				new StatusCallback() {

					@Override
					public void call(com.facebook.Session session,
							SessionState state, Exception exception) {
						// TODO Auto-generated method stub
						statusCallback.call(session, state, exception);
						List<String> permissions = session.getPermissions();
						if (session != null && session.isOpened()
								&& !permissions.containsAll(PERMISSIONS)) {
							requestPublishPermissions(activity, session);
						}
					}
				});
	}

	public static void closeSession() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
			SharedPreferenceUtil
					.removeValue(SharedPreferenceUtil.PREF_FACEBOOK_TOKEN);
			SharedPreferenceUtil
					.removeValue(SharedPreferenceUtil.PREF_FACEBOOK_EXPIRE_DATE);
		}
	}

	public static boolean isOpen() {
		Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			return true;
		}
		return false;
	}

	public static void requestPublishPermissions(Activity activity,
			Session session) {
		if (session != null) {
			Session.NewPermissionsRequest newPermissionRequest = new Session.NewPermissionsRequest(
					activity, PERMISSIONS).setRequestCode(REAUTH_ACTIVITY_CODE);
			session.requestNewPublishPermissions(newPermissionRequest);
		}
	}

	public static void postMessage(final Activity activity,
			final String message, final PostCompleteRunnable completeTask) {
		AsyncTask<Void, Void, Response> task = new AsyncTask<Void, Void, Response>() {

			@Override
			protected Response doInBackground(Void... params) {
				// TODO Auto-generated method stub
				Bundle param = new Bundle();
				param.putString("message", message);
				Request request = new Request(Session.getActiveSession(),
						"/me/feed", param, HttpMethod.POST);
				return request.executeAndWait();
			}

			@Override
			protected void onPostExecute(Response result) {
				// TODO Auto-generated method stub
				PostResponse postResponse = result
						.getGraphObjectAs(PostResponse.class);

				if (postResponse == null || postResponse.getId() == null) {
					handleError(activity, result.getError(), completeTask);
				} else {
					completeTask.init(
							activity.getString(R.string.success_post_facebook),
							true, true, Account.Facebook.getIndex());
				}
				activity.runOnUiThread(completeTask);
			}
		};

		task.execute();
	}

	public static void postPhoto(final Activity activity, final String message,
			final File imageFile, final PostCompleteRunnable completeTask) {
		AsyncTask<Void, Void, Response> task = new AsyncTask<Void, Void, Response>() {

			@Override
			protected Response doInBackground(Void... params) {
				// TODO Auto-generated method stub
				Bitmap bitmap = PhotoUtil.fileToBitmap(imageFile
						.getAbsolutePath());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
				byte[] byteArray = baos.toByteArray();

				Bundle param = new Bundle();
				param.putByteArray("source", byteArray);
				if (message != null && !message.equals("")) {
					param.putString("message", message);
				}
				Request request = new Request(Session.getActiveSession(),
						"/me/photos", param, HttpMethod.POST);
				return request.executeAndWait();
			}

			@Override
			protected void onPostExecute(Response result) {
				// TODO Auto-generated method stub
				PostResponse postResponse = result
						.getGraphObjectAs(PostResponse.class);

				if (postResponse == null || postResponse.getId() == null) {
					handleError(activity, result.getError(), completeTask);

				} else {
					completeTask.init(activity
							.getString(R.string.success_image_post_facebook),
							true, true, Account.Facebook.getIndex());
				}
				activity.runOnUiThread(completeTask);
			}
		};

		task.execute();
	}

	private interface PostResponse extends GraphObject {
		String getId();
	}

	private static void handleError(final Activity activity,
			FacebookRequestError error, PostCompleteRunnable completeTask) {
		String dialogBody = null;
		Resources resources = activity.getResources();

		if (error == null) {
			// There was no response from the server.
			dialogBody = resources.getString(R.string.errorTitle);
		} else {
			switch (error.getCategory()) {
			case AUTHENTICATION_RETRY:
				// Tell the user what happened by getting the
				// message id, and retry the operation later.
				String userAction = (error.shouldNotifyUser()) ? "" : resources
						.getString(error.getUserActionMessageId());
				dialogBody = resources.getString(
						R.string.error_authentication_retry, userAction);
				break;

			case AUTHENTICATION_REOPEN_SESSION:
				// Close the session and reopen it.
				dialogBody = resources
						.getString(R.string.error_authentication_reopen);
				break;

			case PERMISSION:
				// A permissions-related error
				dialogBody = resources.getString(R.string.error_permission);
				break;

			case SERVER:
			case THROTTLING:
				// This is usually temporary, don't clear the fields, and
				// ask the user to try again.
				dialogBody = resources.getString(R.string.error_server);
				break;

			case BAD_REQUEST:
				// This is likely a coding error, ask the user to file a bug.
				dialogBody = resources.getString(R.string.error_bad_request,
						error.getErrorMessage());
				break;

			case OTHER:
			case CLIENT:
			default:
				// An unknown issue occurred, this could be a code error, or
				// a server side issue, log the issue, and either ask the
				// user to retry, or file a bug.
				dialogBody = resources.getString(R.string.error_unknown,
						error.getErrorMessage());
				break;
			}
		}

		// Show the error and pass in the listener so action
		// can be taken, if necessary.
		completeTask.init(dialogBody, true, false, Account.Facebook.getIndex());
	}

	private static class SessionStatusCallback implements
			Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (session != null && session.isOpened()) {
				if (AccountActivity.accountImages != null
						&& AccountActivity.accountListViewAdapter != null) {
					AccountActivity.accountImages[0] = R.drawable.fb_blue;
					AccountActivity.accountListViewAdapter.getItem(0)
							.setItemImageId(R.drawable.fb_blue);
					AccountActivity.accountListViewAdapter
							.notifyDataSetChanged();
				}
				SharedPreferenceUtil.put(
						SharedPreferenceUtil.PREF_FACEBOOK_TOKEN,
						session.getAccessToken());
				SharedPreferenceUtil.put(
						SharedPreferenceUtil.PREF_FACEBOOK_EXPIRE_DATE,
						dateFormat.format(session.getExpirationDate()));
			} else {
				if (AccountActivity.accountImages != null
						&& AccountActivity.accountListViewAdapter != null) {
					AccountActivity.accountImages[0] = R.drawable.fb_white;
					AccountActivity.accountListViewAdapter.getItem(0)
							.setItemImageId(R.drawable.fb_white);
					AccountActivity.accountListViewAdapter
							.notifyDataSetChanged();
				}
				SharedPreferenceUtil
						.removeValue(SharedPreferenceUtil.PREF_FACEBOOK_TOKEN);
				SharedPreferenceUtil
						.removeValue(SharedPreferenceUtil.PREF_FACEBOOK_EXPIRE_DATE);
			}
		}
	}
}
