package com.vmd.webViewControl;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class ActivitySettings extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	/**
	 * The main method of the MainActivity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
			
		updateSummaries();
	}

	/**
	 * Called when the activity will start interacting with the user.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	/**
	 * Called when the system is about to start resuming a previous activity.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	/**
	 * Called when the preferences was changed.
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updateSummaries();
	}
	
	/**
	 * Update preferences summaries.
	 */
	public void updateSummaries() {
		Preference pref = findPreference("startUrl");

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		pref.setSummary(preferences.getString("startUrl", ""));
	}
}
