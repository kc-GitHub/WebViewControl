package com.vmd.cordovaPlugins;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class HeadsetListener extends CordovaPlugin {

	private static final String LOG_TAG = "HeadsetListener";
	
	BroadcastReceiver receiver;
	
	private CallbackContext headsetCallbackContext = null;

	/**
	 * Constructor.
	 */
	public HeadsetListener() {
		this.receiver = null;
	}

	/**
	 * Executes the request.
	 *
	 * @param action			The action to execute.
	 * @param args				JSONArry of arguments for the plugin.
	 * @param callbackContext 	The callback context used when calling back into JavaScript.
	 * @return					True if the action was valid, false if not.
	 */
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
		if (action.equals("start")) {
			if (this.headsetCallbackContext != null) {
				callbackContext.error( "Headset listener already running.");
				return true;
			}
			this.headsetCallbackContext = callbackContext;
		
			// We need to listen to headset events to update headset status
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
			if (this.receiver == null) {
				this.receiver = new BroadcastReceiver() {
					@Override
					public void onReceive(Context arg0, Intent intent) {
						updateHeadsetInfo(intent);
					}
				};
				cordova.getActivity().registerReceiver(this.receiver, intentFilter);
			}
		
			// Don't return any result now, since status results will be sent when events come in from broadcast receiver
			PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
			pluginResult.setKeepCallback(true);
			callbackContext.sendPluginResult(pluginResult);
			return true;
		}

		else if (action.equals("stop")) {
			removeHeadsetListener();
			this.sendUpdate(new JSONObject(), false); // release status callback in JS side
			this.headsetCallbackContext = null;
			callbackContext.success();
			return true;
		}

		return false;
	}

	/**
	 * Stop headset receiver.
	 */
	public void onDestroy() {
		removeHeadsetListener();
	}

	/**
	 * Stop headset receiver.
	 */
	public void onReset() {
		removeHeadsetListener();
	}

	/**
	 * Stop the headset receiver and set it to null.
	 */
	private void removeHeadsetListener() {
		if (this.receiver != null) {
			try {
				this.cordova.getActivity().unregisterReceiver(this.receiver);
				this.receiver = null;
			} catch (Exception e) {
				Log.e(LOG_TAG, "Error unregistering headset receiver: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Updates the JavaScript side whenever the headset state changes
	 *
	 * @param headsetIntent the current battery information
	 * @return
	 */
	private void updateHeadsetInfo(Intent headsetIntent) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("isPlugged", headsetIntent.getIntExtra("state", 0) == 1 ? true : false);
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}

		sendUpdate(obj, true);
	}

	/**
	 * Create a new plugin result and send it back to JavaScript
	 *
	 * @param connection the network info to set as navigator.connection
	 */
	private void sendUpdate(JSONObject info, boolean keepCallback) {
		if (this.headsetCallbackContext != null) {
			PluginResult result = new PluginResult(PluginResult.Status.OK, info);
			result.setKeepCallback(keepCallback);
			this.headsetCallbackContext.sendPluginResult(result);
		}
	}
}
