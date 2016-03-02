package com.vmd.webViewControl.mJpeg;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
	import android.view.WindowManager;
import com.vmd.webViewControl.mJpeg.MjpegInputStream;
import com.vmd.webViewControl.mJpeg.MjpegView;

public class MjpegSample extends Activity {

	private MjpegView mv;
	private static final int MENU_QUIT = 1;

	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {    
		menu.add(0, MENU_QUIT, 0, "Quit");
		return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {    
		switch (item.getItemId()) {
			case MENU_QUIT:
				mv.stopPlayback();
				finish();
				return true;
		}
		return false;
	}

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		String URL = "http://stadtelstra.dyndns.org/mjpg/video.mjpg?camera=1";
		//String URL = "http://k7.dyndns.tv:81/videostream.cgi?user=view&pwd=vie&resolution=4";
//		String URL = "http://192.168.178.253/videostream.cgi?user=view&pwd=test";

//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
		);

		mv = new MjpegView(this);
		setContentView(mv);

		mv.setSource(MjpegInputStream.read(URL));
		mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
		mv.showFps(true);
	}

	public void onPause() {
		super.onPause();
		mv.stopPlayback();
	}
}