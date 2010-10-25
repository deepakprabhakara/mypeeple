package com.mypeeple;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

import com.mypeeple.home.HomeMainActivity;
import com.mypeeple.people.PeopleMainActivity;
import com.mypeeple.places.PlacesMainActivity;
import com.mypeeple.service.MyPeepleService;
import com.mypeeple.events.EventsMainActivity;
import com.mypeeple.settings.SettingsActivity;
import com.mypeeple.utils.Constants;

public class MyPeepleActivity extends TabActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean autolocation = prefs.getBoolean(Constants.PREFS_AUTO_LOCATION, true);

		if(autolocation)
		{
			Intent serviceIntent = new Intent(this, MyPeepleService.class);
			startService(serviceIntent);
		}

		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Reusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, HomeMainActivity.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("home")
				.setIndicator(this.getString(R.string.home), res.getDrawable(R.drawable.ic_tab_home))
				.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, PeopleMainActivity.class);
		spec = tabHost
				.newTabSpec("people")
				.setIndicator(this.getString(R.string.people),
						res.getDrawable(R.drawable.ic_tab_people))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, PlacesMainActivity.class);
		spec = tabHost
				.newTabSpec("places")
				.setIndicator(this.getString(R.string.places),
						res.getDrawable(R.drawable.ic_tab_places))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, EventsMainActivity.class);
		spec = tabHost
				.newTabSpec("events")
				.setIndicator(this.getString(R.string.events),
						res.getDrawable(R.drawable.ic_tab_events))
				.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.settingsMenu:
			Intent myIntent = new Intent(this, SettingsActivity.class);
			this.startActivity(myIntent);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
