package com.mypeeple.db;

import com.mypeeple.R;
import com.mypeeple.dataobj.LocationData;
import com.mypeeple.service.MyPeepleService;
import com.mypeeple.utils.Constants;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedLocationDB {
	Context context;
	SharedPreferences sharedprefs;
	
	static String FIELD_NAME = "name";
	static String FIELD_ADDRESS = "address";
	static String FIELD_LATITUDE = "lat";
	static String FIELD_LONGITUDE = "long";
	static String FIELD_INIT = "init";
	static String FIELD_MAXITEMS = "maxitems";

	public SharedLocationDB(Context context)
	{
		this.context = context;
		sharedprefs = context.getSharedPreferences(Constants.SHARED_LOCATION_DATABASE, 0);
	}
	
	public void writeLocationData(LocationData locationData, boolean init, boolean notifyService)
	{	
		if(locationData.locationid == 0)
		{
			locationData.locationid = getMaxItems() + 1;
			setMaxItems(getMaxItems() + 1);
		}
		
		SharedPreferences.Editor editor = sharedprefs.edit();
	    editor.putString(FIELD_NAME + locationData.locationid, locationData.name);
	    editor.putString(FIELD_ADDRESS + locationData.locationid, locationData.address);
	    editor.putInt(FIELD_LATITUDE + locationData.locationid, locationData.latitudeE6);
	    editor.putInt(FIELD_LONGITUDE + locationData.locationid, locationData.longitudeE6);
	    editor.putBoolean(FIELD_INIT + locationData.locationid, init);
	    // Commit the edits!
	    editor.commit();
	    
	    if(notifyService)
	    	notifyService();
	}

	public LocationData getLocationData(int id)
	{
		LocationData locationData = new LocationData(id, "", "", 0, 0);
		locationData.name = sharedprefs.getString(FIELD_NAME + id, "");
		locationData.address = sharedprefs.getString(FIELD_ADDRESS + id, "");
		locationData.latitudeE6 = sharedprefs.getInt(FIELD_LATITUDE + id, 0);
		locationData.longitudeE6 = sharedprefs.getInt(FIELD_LONGITUDE + id, 0);
		locationData.initialized = sharedprefs.getBoolean(FIELD_INIT + id, false);
		return locationData;
	}
	
	public boolean deleteLocationData(int id)
	{
		int max = getMaxItems();
		LocationData locationData;
		for(int i = id; i < max ; ++i)
		{
			if( (i + 1) <= max)
			{
				locationData = getLocationData(i+1);
				locationData.locationid = i;
				writeLocationData(locationData, true, false);
				locationData = null;
			}
		}
		
		locationData = new LocationData(max, "", "", 0, 0);
		writeLocationData(locationData, false, true);
		locationData = null;
		
		setMaxItems(max - 1);
		
		SharedEventsDB sed = new SharedEventsDB(context);
		sed.deleteEventDataByLocationId(id);
		
		return true;
	}
	
	public int getMaxItems()
	{
		int max = sharedprefs.getInt(FIELD_MAXITEMS, -1);
		if(max == -1)
		{
			setMaxItems(2);
			max = 2;
		}
		
		return max;
	}

	public void setMaxItems(int max)
	{
		SharedPreferences.Editor editor = sharedprefs.edit();
		editor.putInt(FIELD_MAXITEMS, max);
		editor.commit();
	}
	
	void notifyService()
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean autolocation = prefs.getBoolean(Constants.PREFS_AUTO_LOCATION,
				true);

		if (autolocation) {
			Intent serviceIntent = new Intent(context, MyPeepleService.class);
			serviceIntent.putExtra(Constants.INTENT_EXTRAS_LOCATIONCHANGED, true);
			context.startService(serviceIntent);
		}
		
	}
	
    public String[] getLocationsString(boolean view)
    {
    	int max = getMaxItems();
    	String[] mStrings = null;
    	    	
    	int initial = 0;
    	int super_max = max - 1;
    	int offset = 1;
    	
    	if(view)
    	{
    		mStrings = new String[max+1];//{"Add New Place", "My Home", "My Work", ...};
    		mStrings[0] = context.getString(R.string.add_favorite); // "Add New Place"
    		initial = 1;
    		super_max = max;
    		offset = 0;
    	}
    	else
    	{
    		mStrings = new String[max];
    	}
    	
    	LocationData locationData;
    	for(int i = initial; i <= super_max; ++i)
    	{
    		locationData = getLocationData(i + offset);
    		if(locationData.name == "" && i == initial)
    			locationData.name = Constants.PREDEFINED_HOME_LOCATION; // "My Home"
    		if(locationData.name == "" && i == (initial + 1))
    			locationData.name = Constants.PREDEFINED_WORK_LOCATION; // "My Work"

    		mStrings[i] = locationData.name;
    		locationData = null;
    	}
    	return mStrings;
    }
}
