package com.vmd.cordovaPlugins;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * This class echoes a string called from JavaScript.
 */
public class DeviceControl extends CordovaPlugin {
	
	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		final Activity ctxActivity = this.cordova.getActivity();
		final DeviceControl self = this;
		
		if (action.equals("setScreenBrightness")) {
			final int screenBrightness = args.getInt(0);
			cordova.getActivity().runOnUiThread(new Runnable() {
				public void run() {
					self.setScreenBrightness(ctxActivity, screenBrightness);
					callbackContext.success();
				}
			});
		}

		if (action.equals("setKeepScreenOn")) {
			final boolean enable = args.getBoolean(0);
			cordova.getActivity().runOnUiThread(new Runnable() {
				public void run() {
					self.setAppFlag(ctxActivity, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, enable);
					callbackContext.success();
				}
			});
		}

		if (action.equals("setFullscreen")) {
			final boolean enable = args.getBoolean(0);
			cordova.getActivity().runOnUiThread(new Runnable() {
				public void run() {
					self.setAppFlag(ctxActivity, WindowManager.LayoutParams.FLAG_FULLSCREEN, enable);
					callbackContext.success();
				}
			});
		}

		if (action.equals("dimSystemBar")) {
			cordova.getActivity().runOnUiThread(new Runnable() {
				public void run() {
					self.dimSystemBar(ctxActivity);
					callbackContext.success();
				}
			});
		}

		if (action.equals("setVolume")) {
			final int volume = args.getInt(0);
			cordova.getActivity().runOnUiThread(new Runnable() {
				public void run() {
					self.setVolume(ctxActivity, volume);
					callbackContext.success();
				}
			});
		}
				
		boolean debug = true;
		final String toastText = (action.equals("showToast")) ? args.getString(0) : action + ": " + args.getString(0);
		
		if (action.equals("showToast") || debug) {
			cordova.getActivity().runOnUiThread(new Runnable() {
				public void run() {
					self.showToast(ctxActivity, toastText);
					callbackContext.success();
				}
			});
			return true;
		}

		
		/*
		cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				if (action.equals("setScreenBrightness")) {
					self.setScreenBrightness(ctxActivity, paramInt);
				}
				
				if (action.equals("setKeepScreenOn")) {
					self.setAppFlag(ctxActivity, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, paramBool);
				}

				callbackContext.success();
			}
		});
		*/
		
		return false;
	}

	/**
	 * Set APP volume
	 */
	public void setVolume(Activity ctxActivity, int level) {
		AudioManager audioManager = (AudioManager)ctxActivity.getSystemService(Context.AUDIO_SERVICE);
		Integer maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		if (level > maxVolume) {
			this.showToast(ctxActivity, "Warning: Maximum volume level must be " + maxVolume.toString());
		}
		
		level = (level > 0) ? level : 0;
		level = (level > maxVolume) ? maxVolume : level;
		
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, level, AudioManager.FLAG_SHOW_UI);
	}

	
	/**
	 * Set the screen brightness for the this App
	 */
	public void setScreenBrightness(Activity ctxActivity, int level) {
		level = (level > 0) ? level : 1;
		level = (level <= 255) ? level : 255;
		
		float brightLevel = (float)level / 255;
		
		WindowManager.LayoutParams layoutParams = ctxActivity.getWindow().getAttributes();
		layoutParams.screenBrightness = brightLevel;
		ctxActivity.getWindow().setAttributes(layoutParams);
	}

	/**
	 * Set flags for the this App
	 */
	public void setAppFlag(Activity ctxActivity, int flag, boolean enable) {
		if (enable == true) {
			ctxActivity.getWindow().addFlags(flag);
		} else {
			ctxActivity.getWindow().clearFlags(flag);
		}		
	}
	
	/**
	 * Dim out the system bar in ICS or higher
	 */
	public void dimSystemBar(Activity ctxActivity) {
//		ctxActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}
	
	/**
	 * show a short toast messages
	 */
	public void showToast(Activity ctxActivity, String text) {
		Context context = ctxActivity.getApplicationContext();

		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();
	}

		
}