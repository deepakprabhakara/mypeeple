package com.mypeeple.db;

import java.util.Vector;

import com.mypeeple.dataobj.EventData;
import com.mypeeple.service.MyPeepleService;
import com.mypeeple.utils.Constants;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedEventsDB {
	Context context;
	SharedPreferences sharedprefs;

	static String FIELD_EVENTID = "eventid";
	static String FIELD_LOCATIONID = "locationid";
	static String FIELD_TYPE = "type";
	static String FIELD_MESSAGE = "message";
	static String FIELD_RADIUS = "radius";
	static String FIELD_MAXITEMS = "maxitems";
	
	public SharedEventsDB(Context context)
	{
		this.context = context;
		sharedprefs = context.getSharedPreferences(Constants.SHARED_EVENTS_DATABASE, 0);
	}
	
	public void writeEventData(EventData eventData, boolean notifyService)
	{	
		if(eventData.eventid == 0)
		{
			eventData.eventid = getMaxItems() + 1;
			setMaxItems(getMaxItems() + 1);
		}
	
		SharedPreferences.Editor editor = sharedprefs.edit();
	    editor.putInt(FIELD_LOCATIONID + eventData.eventid, eventData.locationid);
	    editor.putString(FIELD_TYPE + eventData.eventid, eventData.type);
	    editor.putString(FIELD_MESSAGE + eventData.eventid, eventData.message);
	    editor.putInt(FIELD_RADIUS + eventData.eventid, eventData.radius);
	    // Commit the edits!
	    editor.commit();
	    
	    if(notifyService)
	    	notifyService();
	}

	public EventData getEventData(int id)
	{
		EventData eventData = new EventData(id, 0, "", "", 0);
		eventData.locationid = sharedprefs.getInt(FIELD_LOCATIONID + id, 0);
		eventData.type = sharedprefs.getString(FIELD_TYPE + id, "");
		eventData.message = sharedprefs.getString(FIELD_MESSAGE + id, "");
		eventData.radius = sharedprefs.getInt(FIELD_RADIUS + id, 0);
		return eventData;
	}
	
	public boolean deleteEventData(int id)
	{
		int max = getMaxItems();
		EventData eventData;
		for(int i = id; i < max ; ++i)
		{
			if( (i + 1) <= max)
			{
				eventData = getEventData(i+1);
				eventData.eventid = i;
				writeEventData(eventData, false);
				eventData = null;
			}
		}
		
		eventData = new EventData(max, 0, "", "", 0);
		writeEventData(eventData, true);
		eventData = null;
		
		setMaxItems(max - 1);

		return true;
	}

	public boolean deleteEventDataByLocationId(int locationid)
	{
		int max;
		EventData eventData;
		Vector<Integer> deleteIds = new Vector<Integer>();
		max = getMaxItems();
		for(int i = 1; i <= max ; ++i)
		{
			eventData = getEventData(i);
			if(eventData.locationid == locationid)
			{
				deleteIds.add(new Integer(i));
			}
		}
		
		for(int i = 0; i < deleteIds.size(); ++i)
		{
			Integer I = deleteIds.get(i);
			deleteEventData(I.intValue());
		}

		return true;
	}

	public int getMaxItems()
	{
		int max = sharedprefs.getInt(FIELD_MAXITEMS, -1);
		if(max == -1)
		{
			setMaxItems(0);
			max = 0;
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
			serviceIntent.putExtra(Constants.INTENT_EXTRAS_EVENTCHANGED, true);
			context.startService(serviceIntent);
		}
		
	}
}
