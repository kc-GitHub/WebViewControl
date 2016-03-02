package com.vmd.webViewControl;

import android.app.AlertDialog;

public class Util extends ActivityMain {

	public void alert(String txt) {
		alert (txt, "Alert Dialog");
	}
	
	
	public void alert(String txt, String title) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();

		// Setting Dialog Title
		alertDialog.setTitle(title);

		// Setting Dialog Message
		alertDialog.setMessage(txt);
		
		alertDialog.setCancelable(true);

		// Showing Alert Message
		alertDialog.show();
	}

}
