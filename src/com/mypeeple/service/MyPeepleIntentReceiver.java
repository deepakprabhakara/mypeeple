package com.mypeeple.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mypeeple.utils.Constants;

public class MyPeepleIntentReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean autolocation = prefs.getBoolean(Constants.PREFS_AUTO_LOCATION, true);

		if (autolocation) {
			Intent serviceIntent = new Intent(context, MyPeepleService.class);
			context.startService(serviceIntent);
		}		
	}
}
