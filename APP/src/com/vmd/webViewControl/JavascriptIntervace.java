package com.vmd.webViewControl;

import org.apache.cordova.DroidGap;
import android.webkit.WebView;

public class JavascriptIntervace { 
//	private WebView mAppView;
//	private DroidGap mGap;
	private String mAppId;

	public JavascriptIntervace(DroidGap gap, WebView view, String appId) {
//		mAppView = view;
//		mGap = gap;
		mAppId = appId;
	}

	public String getAppId(){
		return mAppId;
	}
}
