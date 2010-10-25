package com.mypeeple.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.mypeeple.R;
import com.mypeeple.service.MyPeepleService;
import com.mypeeple.utils.Constants;

public class SettingsActivity extends PreferenceActivity {
	private boolean atomaticlocation;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);
		//getPrefs();
	}

	public void onResume() {
		super.onResume();
		getPrefs();
	}

	public void onPause() {
		super.onPause();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		boolean autolocpref = prefs.getBoolean(Constants.PREFS_AUTO_LOCATION, true);

		if (atomaticlocation == autolocpref)
			return;

		Intent serviceIntent = new Intent(this, MyPeepleService.class);

		if (autolocpref) {
			startService(serviceIntent);
		} else {
			stopService(serviceIntent);
		}
	}

	private void getPrefs() {
		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		atomaticlocation = prefs.getBoolean(Constants.PREFS_AUTO_LOCATION, true);
	}
}
