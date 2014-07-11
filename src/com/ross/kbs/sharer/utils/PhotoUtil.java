package com.ross.kbs.sharer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PhotoUtil {

	private static String selectedImageFileName = "selectedPhoto.png";

	public static String getSelectedImagePath(Activity activity) {
		File f = new File(activity.getExternalFilesDir(null).toString());
		for (File temp : f.listFiles()) {
			if (temp.getName().equals(selectedImageFileName)) {
				return temp.getAbsolutePath();
			}
		}
		return null;
	}

	public static File getSelectedImageFile(Activity activity) {
		String path = PhotoUtil.getSelectedImagePath(activity);
		return (path != null) ? new File(
				PhotoUtil.getSelectedImagePath(activity)) : null;
	}

	public static File createTempImageFile(Activity activity) {
		File storageDir = activity.getExternalFilesDir(null);
		File file = new File(storageDir, selectedImageFileName);
		if (file.exists()) {
			file.delete();
			file = new File(storageDir, selectedImageFileName);
		}
		return file;
	}

	public static File createImageFile(Activity activity, Bitmap bmp)
			throws IOException {
		// Create an image file name
		OutputStream outStream = null;
		File file = PhotoUtil.createTempImageFile(activity);

		try {
			outStream = new FileOutputStream(file);
			bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
			outStream.flush();
			outStream.close();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return file;
	}

	public static Bitmap scaleImage(String path, int targetW, int targetH) {
		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		return BitmapFactory.decodeFile(path, bmOptions);
	}

	public static Bitmap fileToBitmap(String path) {
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		return BitmapFactory.decodeFile(path, bmOptions);
	}

	public static void deleteImageFile(Activity activity) {
		File storageDir = activity.getExternalFilesDir(null);
		File file = new File(storageDir, selectedImageFileName);
		if (file.exists()) {
			file.delete();
		}
	}

	public static boolean isExistImageFile(Activity activity) {
		File storageDir = activity.getExternalFilesDir(null);
		File file = new File(storageDir, selectedImageFileName);
		return file.exists();
	}
}
