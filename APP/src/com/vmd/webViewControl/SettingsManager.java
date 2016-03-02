package com.vmd.webViewControl;

import org.apache.cordova.DroidGap;

import com.vmd.cordovaPlugins.DeviceControl;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.webkit.WebSettings;
import org.apache.cordova.*;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;

public class SettingsManager {
	protected SharedPreferences preferences;
	protected DroidGap parentActivity;
	protected DeviceControl deviceControl;
	protected int currentApiVersion = android.os.Build.VERSION.SDK_INT;

	public SettingsManager(ActivityMain activityMain) {
		parentActivity = activityMain;

		// load preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(activityMain);
		deviceControl = new DeviceControl();
	}

	/**
	 * Apply app settings from preferences
	 */
	protected void init() {
		if (preferences.getBoolean("enableFullscreen", false)) {
			parentActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			parentActivity.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN
			);
		}

		if (preferences.getBoolean("autostart", false)) {
			// http://www.androidpit.de/de/android/forum/thread/457574/Laufender-Service-bei-Tastensperre
			// disable keyguard (only if it is not a secure lock keyguard)
			deviceControl.setAppFlag(parentActivity, WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD, true);

			// let windows be shown when the screen is locked
			deviceControl.setAppFlag(parentActivity, WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, true);
		}
		

		// Time in milliseconds to wait before triggering a timeout error when loading  with super.loadUrl().
		String loadUrlTimeoutValue = preferences.getString("loadUrlTimeoutValue", "20000");
		parentActivity.setIntegerProperty("loadUrlTimeoutValue", Integer.parseInt(loadUrlTimeoutValue));
		
		/***********************************************************************
		 * Zoom Settings
		 ***********************************************************************/
		// Enable full screen width in Viewport
		((ActivityMain) parentActivity).getAppView().getSettings().setUseWideViewPort(preferences.getBoolean("enableQuickZoom", false));

		// Start in Overview
		// this.appView.getSettings().setLoadWithOverviewMode(true);	// Sets whether the WebView loads pages in overview mode.

		// Zoom density on 100%: CLOSE=120dpi, MEDIUM=160dpi, FAR=240dpi
		((ActivityMain) parentActivity).getAppView().getSettings().setDefaultZoom(
			WebSettings.ZoomDensity.valueOf(preferences.getString("defaultZoom", "MEDIUM"))
		);	// Sets the default zoom density of the page.
		
		((ActivityMain) parentActivity).getAppView().setInitialScale(
			Integer.parseInt(preferences.getString ("initialZoom", "0"))
		);
		
		// default zoom properties
		((ActivityMain) parentActivity).getAppView().getSettings().setBuiltInZoomControls(true);	// pinch Zoom (Sets whether the WebView should use its built-in zoom mechanisms.)
		

		// Sets whether the WebView should support zooming using its on-screen zoom controls and gestures.
		((ActivityMain) parentActivity).getAppView().getSettings().setSupportZoom(preferences.getBoolean("enableZoom", false));
	}

	@TargetApi(11)
	protected void setDisplayZoomControls(boolean enable) {
		if (currentApiVersion > android.os.Build.VERSION_CODES.GINGERBREAD_MR1){
			((ActivityMain) parentActivity).getAppView().getSettings().setDisplayZoomControls(enable);	// disable zoom buttons
		}
	}
	
	
	
	
	
	public boolean getBoolean(String key, boolean defaultValue) {
		return preferences.getBoolean(key, defaultValue);
	}
	
	public String getString(String key, String defaultValue) {
		return preferences.getString(key, defaultValue);
	}
}
