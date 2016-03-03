package com.vmd.webViewControl;

import android.content.*;
import android.preference.PreferenceManager;

/**
 * Receiver class to receive the BOOT_COMPLETED action
 * 
 * @author Dirk Hoffmann <hoffmann@vmd-jena.de>
 */
public class ReceiverBootComplete extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (preferences.getBoolean("autostart", false)) {
			Intent droidGapIntent = new Intent(context, ActivityMain.class);
			droidGapIntent .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(droidGapIntent);
		}
	}
}
