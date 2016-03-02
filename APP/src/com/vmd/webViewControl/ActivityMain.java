package com.vmd.webViewControl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.cordova.*;

import com.vmd.cordovaPlugins.DeviceControl;
//import com.vmd.webViewControl.mJpeg.MjpegSample;
import com.vmd.webViewControl.mJpeg.MjpegInputStream;
import com.vmd.webViewControl.mJpeg.MjpegView;
import com.vmd.webViewControl.motionDetection.Preview;
	
public class ActivityMain extends DroidGap  {

	protected DeviceControl deviceControl;
	protected JavascriptIntervace jsInterface;
	protected String LogTag = "ActivityMain";
	protected PowerManager powerManager;
	protected PowerManager.WakeLock wakeLock;
	protected PopupWindow popUp;
	private MjpegView mv;
	protected SettingsManager settingsManager;
	
	protected TextView mTextView;

	private BroadcastReceiver mBroadcastReceiveriver;

	/**
	 * The main method of the MainActivity
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.appInit();
	}

	/**
	 * Make some initial settings
	 */
	protected void appInit() {
		// Initialize the activity
		super.init();

		settingsManager = new SettingsManager(this);
		deviceControl = new DeviceControl();

		super.setStringProperty("loadingDialog", "Bitte warten...");
		
		
		// TODO: Settings
		// enable Plugins
		this.appView.getSettings().setPluginState(WebSettings.PluginState.ON);
		
		
		// TODO: Settings
		// Enable PARTIAL_WAKE_LOCK (if Activated: CPU, network (maybe?) still Running at Standby
		this.powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.wakeLock = this.powerManager.newWakeLock(
			PowerManager.PARTIAL_WAKE_LOCK, "webViewControllMainActivity"
		);
		this.wakeLock.acquire();

		
		// TODO: later
//		this.appView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		
		// fade out the softkeys
//		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
		// Cause all links on web page to be loaded into existing web view, instead of being loaded into new browser.
		this.setBooleanProperty("loadInWebView", true);

		// TODO: maybe??? make controllable by settings	
		// Enable App to keep running in background.
		this.setBooleanProperty("keepRunning", true);
		
		settingsManager.init();
		
		// Register javascript interface
		jsInterface = new JavascriptIntervace(this, super.appView, settingsManager.getString("appId", "00000"));
		super.appView.addJavascriptInterface(jsInterface, "appInterface");

		// catch all touch events
		super.appView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				boolean retVal = true;
				// Todo check if device is locked 
				//retVal = false;
				
				
				
				
				
//				if (v.getId() == R.id.web && event.getAction() == MotionEvent.ACTION_DOWN){
				if (event.getAction() == MotionEvent.ACTION_DOWN){

					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder
						.setMessage("Bitte Zugangscode eingeben")
						.setTitle("Interface locked");
					
					builder.setPositiveButton(R.string.dialog_submit, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User clicked OK button
						}
					});
					builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User cancelled the dialog
						}
					});
				
					AlertDialog alertDialog = builder.create();
					alertDialog.show();					

/*					
					// get prompts.xml view
					LayoutInflater li = getActivity().getLayoutInflater();
					View promptsView = li.inflate(R.layout.login_dialog, null);
	 
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
	 
					// set prompts.xml to alertdialog builder
					alertDialogBuilder.setView(promptsView);
	 
					// set dialog message
					alertDialogBuilder
						.setCancelable(false)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								// action on ok
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
							dialog.cancel();
							}
						});
	 
					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();
	 
					// show it
					alertDialog.show();					
*/
					
					
					
					Log.d(LogTag, alertDialog.toString());
				}

				return retVal;
			}
		});

		
		loadStartPage();
	}
	
	/**
	 * Load the start page defined in app settings.
	 * Set user name and password
	 */
	protected void loadStartPage() {
		if (settingsManager.getBoolean("clearCacheOnStartAndReload", false)) {
			localClearCache();
		}

		String username = settingsManager.getString("username", "");
		String password = settingsManager.getString("password", "");
		String startUrl = settingsManager.getString("startUrl", "");
		
		if (startUrl.trim() == "") {
			startUrl = "file:///android_asset/www/index.html";
		}

		Uri uri = Uri.parse(startUrl);
		String scheme = uri.getScheme();
		String host = host = uri.getHost();

//		startUrl = (scheme != null) ? startUrl : "http://" + startUrl;
//		uri = Uri.parse(startUrl);

//		Log.d(LogTag, "---------" + scheme + " || " + startUrl);
		
		// check if we should send username and password authentication
		if (username != "" || password != "") {

			Integer port = uri.getPort();
			if (port == -1) {
				port = (scheme!= null && scheme.equals("https")) ? 443 : 80;
			}

			if (host != null) {
				host = uri.getHost() + ":" + port.toString();
	
//				Log.d(LogTag, "---------" + host);
				
				// token for the authentication
				AuthenticationToken authToken = new AuthenticationToken();
				authToken.setUserName(username);
				authToken.setPassword(password);
	
				super.setAuthenticationToken(authToken, host, null);
			}
		}
		
//		Log.d(LogTag, "---------" + startUrl);
		super.loadUrl(startUrl);
		startMotionDetection();
	}

	/**
	 * Clear the resource cache.
	 */
	protected void localClearCache() {
		this.appView.clearCache(true);
		Toast.makeText(getApplicationContext(), "cache cleared", Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Display default page on load errors
	 * @param errorCode
	 * @param description
	 * @param failingUrl
	 */
	public void onReceivedError( int errorCode, String description, String failingUrl) {
		Log.d(LogTag, "onReceivedError");

		super.spinnerStop();
		super.setStringProperty("loadingDialog", null);

		super.loadUrl(
			"file:///android_asset/www/index.html?notFound=" + failingUrl + "&descr=" + description + "&errorCode=" + errorCode
		);
	}
	
	/**
	 * Override Back key behavior
	 */
	@Override
	public void onBackPressed() {
		popUpDemiss();
		
		// disable back key
		return;
	}
	
	/**
	 * Called when the activity will start interacting with the user.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		final DeviceControl deviceControl = new DeviceControl();
		final Activity thisActivity = this;
		
		// Register filter for our BroadcastReceiver
		IntentFilter intentFilterExternalPowerConnected = new IntentFilter(
			"android.intent.action.ACTION_POWER_CONNECTED"
		);
		IntentFilter intentFilterExternalPowerDisconnected = new IntentFilter(
			"android.intent.action.ACTION_POWER_DISCONNECTED"
		);

		mBroadcastReceiveriver = new BroadcastReceiver() {
			@Override
			/**
			 * Process BroadcastReceiver
			 */
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				if (action.equals(Intent.ACTION_POWER_CONNECTED) && settingsManager.getBoolean("screenKeepOn", false)) {
					deviceControl.setAppFlag(thisActivity, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, true);
				} else {
					deviceControl.setAppFlag(thisActivity, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, false);
				}
			}
		};

		// Registering our BroadcastReceiver
		this.registerReceiver(mBroadcastReceiveriver, intentFilterExternalPowerConnected);
		this.registerReceiver(mBroadcastReceiveriver, intentFilterExternalPowerDisconnected);
		
		// Check ExternalPower connection state
		if (isExternalPowerConnected(getContext()) && settingsManager.getBoolean("screenKeepOn", false)) {
			deviceControl.setAppFlag(thisActivity, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, true);
		} else {
			deviceControl.setAppFlag(thisActivity, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, false);
		}
//		Toast.makeText(getContext(), "resume", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Called when the system is about to start resuming a previous activity.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		
		// Unregister our BroadcastReceiveriver
		this.unregisterReceiver(this.mBroadcastReceiveriver);
		popUpDemiss();
	}

	/**
	 * The final call you receive before your activity is destroyed.
	 */
	@Override
	public void onDestroy() {
		if (this.wakeLock instanceof PowerManager.WakeLock) {
			this.wakeLock.release();
		}

		super.onDestroy();
	}
	
	/**
	 * Declaring the Menu options
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);
		
		return true;
	}

	/**
	 * Handle menu item selection
	 * 
	 * @param item
	 * @return
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		DeviceControl deviceControl = new DeviceControl();

		switch(item.getItemId()){
			case R.id.settings:
				Intent intent = new Intent(ActivityMain.this, ActivitySettings.class);
				startActivity(intent);
				break;
	
			case R.id.exit:
				finish();
				break;
	
			case R.id.reload:
				Toast.makeText(getApplicationContext(), "Invoke reload", Toast.LENGTH_SHORT).show();
				loadStartPage();
				break;
	
			case R.id.clearCache:
				localClearCache();
				break;
	
			case R.id.b50:
				// Screen Brightness 50
				deviceControl.setScreenBrightness(this.getActivity(), 50);
				break;
	
			case R.id.b100:
				// Screen Brightness 100
				deviceControl.setScreenBrightness(this.getActivity(), 100);
				break;

			case R.id.test:
//				Intent mJpegView = new Intent(ActivityMain.this, MjpegSample.class);
//				startActivity(mJpegView);

//				showPopup();
				startMotionDetection();
				break;
		}
		
/*		
		if (item.hasSubMenu() == false) {
			if(item.getTitle() == "Startpage settings") {
				super.loadUrl("javascript:app.startPageSettings()");
			}
		}
*/
		return true;
		
	}
	
	/**
	 * Check if external power connected
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isExternalPowerConnected(Context context) {
		Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
	}
	
	/**
	 * Show or hide developer menu items
	 * 
	 * @param menu
	 */
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		boolean visible = true;
		if (!settingsManager.getBoolean("showDeveloperOptions", false)) {
			visible = false;
		}
	//	menu.findItem(R.id.reload).setVisible(visible);
		menu.findItem(R.id.clearCache).setVisible(visible);
		menu.findItem(R.id.b50).setVisible(false);
		menu.findItem(R.id.b100).setVisible(false);
		menu.findItem(R.id.test).setVisible(true);

		return true;
	}
	
	private void showPopup() {
		LinearLayout mainLayout = new LinearLayout(this);
		TextView tv = new TextView(this);
		LinearLayout layout = new LinearLayout(this);
		
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layout.setOrientation(LinearLayout.VERTICAL);
		tv.setText("Hi this is a sample text for popup window");
		layout.addView(tv, params);

		mv = new MjpegView(this);
		layout.addView(mv, params);

		popUp = new PopupWindow(this);
//		popUp.setBackgroundDrawable(new BitmapDrawable());
		popUp.setContentView(layout);
		popUp.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
		popUp.update(0, 0, 400, 400);
		
//*****************************************************************************	
//		String URL = "http://k7.dyndns.tv:81/videostream.cgi?user=view&pwd=test&resolution=4";
		String URL = "http://stadtelstra.dyndns.org/mjpg/video.mjpg?camera=1";

//		setContentView(mv);

		mv.setSource(MjpegInputStream.read(URL));
		mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
		mv.showFps(true);
//*****************************************************************************
	}

	public interface motionDetectCallback {
		void motionDetect(int response);
	}
	
	private void startMotionDetection() {
		FrameLayout layout = new FrameLayout(this);
		this.getAppView().addView(layout, new LayoutParams(1,1));
		layout.setVisibility(1);

		mTextView = new TextView(this);
		mTextView.setTextColor(Color.RED);
		this.getAppView().addView(mTextView, new LayoutParams(500,100));
		
		Preview preview = new Preview(
			this,
			new motionDetectCallback() {
				public void motionDetect(int response) {
					String txtResponse = Integer.toString(response);
					mTextView.setText(txtResponse);
					Log.i(TAG, txtResponse);
				}
			}
		);
		layout.addView(preview);
	}
	
	private void popUpDemiss() {
		if(popUp != null) {
			mv.stopPlayback();
			popUp.dismiss();
		}
	}
	
	public CordovaWebView getAppView() {
		return this.appView;
	}
}