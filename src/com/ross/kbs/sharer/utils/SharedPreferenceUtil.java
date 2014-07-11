package com.ross.kbs.sharer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferenceUtil {

	public final static String PREF_FACEBOOK_TOKEN = "PREF_FACEBOOK_TOKEN";
	public final static String PREF_FACEBOOK_EXPIRE_DATE = "PREF_FACEBOOK_EXPIRE_DATE";
	public final static String PREF_TWITTER_TOKEN = "PREF_TWITTER_TOKEN";
	public final static String PREF_TWITTER_TOKEN_SECRET = "PREF_TWITTER_TOKEN_SECRET";
	private static Context mContext;

	public static void setGlobalContext(Context c) {
		mContext = c;
	}

	public static void put(String key, String value) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = pref.edit();

		editor.putString(key, value);
		editor.commit();
	}

	public static void put(String key, boolean value) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = pref.edit();

		editor.putBoolean(key, value);
		editor.commit();
	}

	public static void put(String key, int value) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = pref.edit();

		editor.putInt(key, value);
		editor.commit();
	}

	public static String getValue(String key, String dftValue) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		try {
			return pref.getString(key, dftValue);
		} catch (Exception e) {
			return dftValue;
		}

	}

	public static int getValue(String key, int dftValue) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		try {
			return pref.getInt(key, dftValue);
		} catch (Exception e) {
			return dftValue;
		}

	}

	public static boolean getValue(String key, boolean dftValue) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		try {
			return pref.getBoolean(key, dftValue);
		} catch (Exception e) {
			return dftValue;
		}
	}

	public static void removeValue(String key) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		pref.edit().remove(key).commit();
	}
}
