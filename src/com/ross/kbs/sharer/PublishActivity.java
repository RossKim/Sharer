package com.ross.kbs.sharer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.facebook.Session;
import com.ross.kbs.sharer.models.Account;
import com.ross.kbs.sharer.utils.FacebookUtil;
import com.ross.kbs.sharer.utils.KakaoUtil;
import com.ross.kbs.sharer.utils.PhotoUtil;
import com.ross.kbs.sharer.utils.PostCompleteRunnable;
import com.ross.kbs.sharer.utils.SharedPreferenceUtil;
import com.ross.kbs.sharer.utils.TwitterUtil;

public class PublishActivity extends Activity implements LocationListener {

	private ProgressDialog progessDialog;
	private List<Boolean> completePostBooleanList;
	private List<Boolean> successPostList;
	private List<String> completePostMessageList;
	private Timer locationTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_publish);
		SharedPreferenceUtil.setGlobalContext(getApplicationContext());

		FacebookUtil.restoreSession(this, savedInstanceState);
		com.kakao.Session.initializeSession(this, KakaoUtil.sessionCallback);
		if (savedInstanceState != null) {
			setImageViewFromFile();
		}
	}

	public void showAccountActivity(View view) {
		Intent intent = new Intent(this, AccountActivity.class);
		startActivity(intent);
	}

	private final int IMAGE_PICKER_GALLERY = 1001;
	private final int IAMGE_PICKER_CAMERA = 1002;

	public void setImageViewFromFile() {
		ImageView imageView = (ImageView) findViewById(R.id.imageView);
		String path = PhotoUtil.getSelectedImagePath(this);
		if (path != null) {
			Bitmap imageBitmap = PhotoUtil.scaleImage(path,
					imageView.getWidth(), imageView.getHeight());
			imageView.setImageBitmap(imageBitmap);
		}
	}

	public void getImageFromGallery(View view) {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, IMAGE_PICKER_GALLERY);
	}

	public void getImageFromCamera(View view) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (intent.resolveActivity(getPackageManager()) != null) {
			File file = PhotoUtil.createTempImageFile(this);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(intent, IAMGE_PICKER_CAMERA);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case IMAGE_PICKER_GALLERY:
			if (resultCode == RESULT_OK) {
				try {
					final Uri imageUri = data.getData();
					final InputStream imageStream = getContentResolver()
							.openInputStream(imageUri);
					final Bitmap selectedImage = BitmapFactory
							.decodeStream(imageStream);
					PhotoUtil.createImageFile(this, selectedImage);
					((ImageView) findViewById(R.id.imageView))
							.setImageBitmap(selectedImage);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case IAMGE_PICKER_CAMERA:
			if (resultCode == RESULT_OK) {
				setImageViewFromFile();
			}
			break;
		}

		if (Session.getActiveSession() != null) {
			Session.getActiveSession().onActivityResult(this, requestCode,
					resultCode, data);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		com.facebook.Session session = com.facebook.Session.getActiveSession();
		com.facebook.Session.saveSession(session, outState);
	}

	public void deleteImage(View view) {
		final Activity activity = this;
		if (PhotoUtil.isExistImageFile(activity)) {
			AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
			alert_confirm
					.setTitle(R.string.confirmText)
					.setMessage(R.string.confirm_delete_image_text)
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									ImageView imageView = (ImageView) findViewById(R.id.imageView);
									imageView.setImageBitmap(null);
									PhotoUtil.deleteImageFile(activity);
								}
							}).setNegativeButton("No", null);
			AlertDialog alert = alert_confirm.create();
			alert.show();
		}
	}

	public void postMessage(View view) {
		String message = ((EditText) findViewById(R.id.messageView)).getText()
				.toString();
		File imageFile = PhotoUtil.getSelectedImageFile(this);

		if (imageFile == null && message == null) {
			new AlertDialog.Builder(this)
					.setPositiveButton(R.string.error_dialog_button_text, null)
					.setTitle(R.string.errorTitle)
					.setMessage("Please select photo or input message,").show();
			return;
		}

		progessDialog = ProgressDialog.show(this, "",
				getResources().getString(R.string.postProgessText), true);
		completePostBooleanList = Arrays.asList(!FacebookUtil.isOpen(),
				!TwitterUtil.hasAccessToken(), !com.kakao.Session
						.getCurrentSession().isOpened());
		completePostMessageList = Arrays.asList(null, null, null);
		successPostList = Arrays.asList(FacebookUtil.isOpen(), TwitterUtil
				.hasAccessToken(), com.kakao.Session.getCurrentSession()
				.isOpened());

		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1, 0, this);
		}
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}

		locationTimer = new Timer(true);
		final Handler handler = new Handler();
		locationTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						onLocationChanged(null);
						locationTimer.cancel();
					}
				});
			}
		}, 10000, 10000);
	}

	public void postToSNS(Location location) {
		String message = ((EditText) findViewById(R.id.messageView)).getText()
				.toString();
		File imageFile = PhotoUtil.getSelectedImageFile(this);

		if (!completePostBooleanList.get(Account.Facebook.getIndex())) {
			if (imageFile == null) {
				FacebookUtil.postMessage(this, message,
						new PostCompleteRunnable(this));
			} else {
				FacebookUtil.postPhoto(this, message, imageFile,
						new PostCompleteRunnable(this));
			}
		}
		if (!completePostBooleanList.get(Account.Twitter.getIndex())) {
			if (imageFile == null) {
				TwitterUtil.postMessage(this, message, location,
						new PostCompleteRunnable(this));
			} else {
				TwitterUtil.postPhoto(this, message, imageFile, location,
						new PostCompleteRunnable(this));
			}
		}
		if (!completePostBooleanList.get(Account.KakaoStory.getIndex())) {
			if (imageFile == null) {
				KakaoUtil.postMessage(this, message, new PostCompleteRunnable(
						this));
			} else {
				KakaoUtil.postPhoto(this, message, imageFile,
						new PostCompleteRunnable(this));
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		locationTimer.cancel();
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.removeUpdates(this);
		postToSNS(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Log.i("Sharer.Location", "Disabled");
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		// not use
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		// not use
	}

	public void changePostStatus(String message, boolean result,
			boolean success, int index) {
		if (!completePostBooleanList.get(index)) {
			completePostBooleanList.set(index, result);
			completePostMessageList.set(index, message);
			successPostList.set(index, success);
		}
		for (boolean r : completePostBooleanList) {
			if (!r) {
				return;
			}
		}
		postComplete();
	}

	public void postComplete() {
		boolean success = false;
		for (boolean s : successPostList) {
			if (s) {
				success = s;
			}
		}

		if (success) {
			((EditText) findViewById(R.id.messageView)).setText("");
			ImageView imageView = (ImageView) findViewById(R.id.imageView);
			imageView.setImageBitmap(null);
			PhotoUtil.deleteImageFile(this);
		}

		progessDialog.dismiss();
		progessDialog = null;

		String message = "";
		for (String s : completePostMessageList) {
			if (s != null) {
				message += "・" + s + "\n";
			}
		}

		AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
		alert_confirm
				.setTitle(R.string.dialog_title)
				.setMessage(message)
				.setCancelable(false)
				.setNegativeButton(
						getString(R.string.error_dialog_button_text), null);
		AlertDialog alert = alert_confirm.create();
		alert.show();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.removeUpdates(this);
	}
}
