package com.vmd.cordovaPlugins;

import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

/**
 * @author Dirk Hoffmann
 *
 */
public class VoiceRecognition extends CordovaPlugin implements RecognitionListener {

	private static final String LOG_TAG = "VoiceRecognition";
	
	private CallbackContext voiceRecognitionCallbackContext = null;

	private SpeechRecognizer recognizer;
	
	public static final int STATE_RECOGNISE_END = 0;
	public static final int STATE_RECOGNISE_READY = 1;
	public static final int STATE_RECOGNISE_BEGIN = 2;
	public static final int STATE_RECOGNISE_RESULTS = 3;
	public static final int STATE_RECOGNISE_ERROR = 9;
	
	/**
	 * Constructor.
	 */
	public VoiceRecognition() {
	}
	
	/**
	 * Phonegap plugin request executor
	 *
	 * @param action			The action to execute.
	 * @param args				JSONArry of arguments for the plugin.
	 * @param callbackContext	The callback context used when calling back into JavaScript.
	 * @return					True if the action was valid, false if not.
	 * @throws					JSONException 
	 */
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		final Activity ctxActivity = this.cordova.getActivity();

		if (action.equals("start")) {
			if (this.voiceRecognitionCallbackContext != null) {
				callbackContext.error( "Voice recognition listener already running.");
				return true;
			}
			this.voiceRecognitionCallbackContext = callbackContext;
			
			// Don't return any result now, since status results will be sent when events come in from broadcast receiver
			PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
			pluginResult.setKeepCallback(true);
			callbackContext.sendPluginResult(pluginResult);
			return true;

		} else if (action.equals("stop")) {
			Log.d(LOG_TAG, "Unregistering voice recognition listener");

			 // release status callback in JS side
			this.sendUpdate(new JSONObject(), false);
			this.voiceRecognitionCallbackContext = null;
			callbackContext.success();
			return true;

		} else if (action.equals("init")) {
			Log.d(LOG_TAG, "Init voice recognition service");

			// Check to see if a recognition activity is present
			PackageManager pm = ctxActivity.getPackageManager();
			List<ResolveInfo> activities = pm.queryIntentActivities(
				new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0
			);

			if (activities.size() != 0) {
				callbackContext.success();
			} else {
				callbackContext.error("Sorry, voice recognition not present on your Device.");
			}
			return true;

		} else if (action.equals("startRecognition")) {
			Log.d(LOG_TAG, "Start voice recognition service");
			cordova.getActivity().runOnUiThread(new Runnable() {
				public void run() {
					Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		//			Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
					
					if (!intent.hasExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE)) {
						intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.dummy");
					}
					
					SpeechRecognizer recognizer = getSpeechRecognizer();
					recognizer.startListening(intent);
				}
			});
			return true;
		}

		boolean debug = true;
		final String toastText = (action.equals("showToast")) ? args.getString(0) : action + ": " + args.getString(0);
		
		if (debug) {
			DeviceControl deviceControl = new DeviceControl();
			deviceControl.showToast(ctxActivity, toastText);
		}
		
		return false;
	}
	
	/**
	 * @param state
	 */
	private void setRecognizerStatus(Integer state) {
		setRecognizerStatus(state, "");
	}

	/**
	 * @param state
	 * @param result
	 */
	private void setRecognizerStatus(Integer state, String result) {
		JSONObject obj = new JSONObject();
		int errorCode = 0;
		try {
			if (state == STATE_RECOGNISE_ERROR) {
				errorCode = Integer.parseInt(result);
			}
			if (state != STATE_RECOGNISE_RESULTS) {
				result = "";
			}
			
			obj.put("state", state);
			obj.put("errorCode", errorCode);
			obj.put("result", result);
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
		if (this.voiceRecognitionCallbackContext != null) {
			PluginResult result = new PluginResult(PluginResult.Status.OK, info);
			result.setKeepCallback(keepCallback);
			this.voiceRecognitionCallbackContext.sendPluginResult(result);
		}
	}	
	
	/**
	 * Initialize the speech recognizer
	 */
	private SpeechRecognizer getSpeechRecognizer() {
		if (recognizer == null) {
			recognizer = SpeechRecognizer.createSpeechRecognizer(this.cordova.getActivity());
			recognizer.setRecognitionListener(this);
		}
		return recognizer;
	}
	
	/**************************************************************************
	/* Interface Methods
	 *************************************************************************/
	@Override
	public void onEndOfSpeech() {
		Log.d(LOG_TAG, "onEndOfSpeech");
		setRecognizerStatus(STATE_RECOGNISE_END);
	}

	@Override
	public void onBeginningOfSpeech() {
		Log.d(LOG_TAG, "onBeginningOfSpeech");
		setRecognizerStatus(STATE_RECOGNISE_BEGIN);
	}

	@Override
	public void onError(int error) {
		Log.d(LOG_TAG, "recognitionFailure: " + error);
		setRecognizerStatus(STATE_RECOGNISE_ERROR, "" + error);
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		Log.d(LOG_TAG, "onReadyForSpeech");
		setRecognizerStatus(STATE_RECOGNISE_READY);
	}

	@Override
	public void onResults(Bundle results) {
		ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		String recognitionWord = matches.get(0);
		Log.d(LOG_TAG, "onResults: " + recognitionWord);
		setRecognizerStatus(STATE_RECOGNISE_RESULTS, recognitionWord);
	}

	
	@Override
	public void onBufferReceived(byte[] buffer) {}

	@Override
	public void onEvent(int eventType, Bundle params) {}

	@Override
	public void onPartialResults(Bundle partialResults) {}

	@Override
	public void onRmsChanged(float rmsdB) {}
}