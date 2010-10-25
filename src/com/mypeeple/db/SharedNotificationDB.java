package com.mypeeple.db;

import com.mypeeple.dataobj.NotificationData;
import com.mypeeple.utils.Constants;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedNotificationDB {
	Context context;
	SharedPreferences sharedprefs;
	
	static String FIELD_LOCATIONID = "locationid";
	static String FIELD_LOCATIONNAME = "locationname";
	static String FIELD_PEOPLEID = "peopleid";
	static String FIELD_TYPE = "type";
	static String FIELD_ADDITIONALMESSAGE = "additionalmessage";
	static String FIELD_TIMESTAMP = "timestamp";
	static String FIELD_MAXITEMS = "maxitems";
	
	public SharedNotificationDB(Context context)
	{
		this.context = context;
		sharedprefs = context.getSharedPreferences(Constants.SHARED_NOTIFICATIONS_DATABASE, 0);
	}
	
	public void writeNotificationData(NotificationData notificationData)
	{	
		if(notificationData.notificationid == 0)
		{
			notificationData.notificationid = getMaxItems() + 1;
			setMaxItems(getMaxItems() + 1);
		}
		
		SharedPreferences.Editor editor = sharedprefs.edit();
	    editor.putInt(FIELD_LOCATIONID + notificationData.notificationid, notificationData.locationid);
	    editor.putString(FIELD_LOCATIONNAME + notificationData.notificationid, notificationData.locationname);
	    editor.putInt(FIELD_PEOPLEID + notificationData.notificationid, notificationData.peopleid);
	    editor.putString(FIELD_TYPE + notificationData.notificationid, notificationData.type);
	    editor.putString(FIELD_ADDITIONALMESSAGE + notificationData.notificationid, notificationData.additionalmessage);
	    String times = String.valueOf(notificationData.timestamp);
	    editor.putString(FIELD_TIMESTAMP + notificationData.notificationid, times);
	    // Commit the edits!
	    editor.commit();
	    
	}

	public NotificationData getNotificationData(int id)
	{
		NotificationData notificationData = new NotificationData(id, 0, "", 0, "", "", 0);
		notificationData.locationid = sharedprefs.getInt(FIELD_LOCATIONID + id, 0);
		notificationData.locationname = sharedprefs.getString(FIELD_LOCATIONNAME + id, "");
		notificationData.peopleid = sharedprefs.getInt(FIELD_PEOPLEID + id, 0);
		notificationData.type = sharedprefs.getString(FIELD_TYPE + id, "");
		notificationData.additionalmessage = sharedprefs.getString(FIELD_ADDITIONALMESSAGE + id, "");
		String times = sharedprefs.getString(FIELD_TIMESTAMP + id, "0");
		notificationData.timestamp = Long.parseLong(times);
		return notificationData;
	}
	
	public boolean deleteNotificationData(int id)
	{
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
}
