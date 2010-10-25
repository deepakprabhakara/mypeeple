package com.mypeeple.db;

import com.mypeeple.dataobj.PeopleData;
import com.mypeeple.utils.Constants;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPeopleDB {
	Context context;
	SharedPreferences sharedprefs;
	
	static String FIELD_CONTACTID = "contactid";
	static String FIELD_NAME = "name";
	static String FIELD_NUMBER = "number";
	static String FIELD_COUNT = "count";
	static String FIELD_MAXITEMS = "maxitems";
	
	public SharedPeopleDB(Context context)
	{
		this.context = context;
		sharedprefs = context.getSharedPreferences(Constants.SHARED_PEOPLE_DATABASE, 0);
	}
	
	public void writePeopleData(PeopleData peopleData)
	{	
		if(peopleData.peopleid == 0)
		{
			peopleData.peopleid = getMaxItems() + 1;
			setMaxItems(getMaxItems() + 1);
		}
	
		SharedPreferences.Editor editor = sharedprefs.edit();
	    editor.putInt(FIELD_CONTACTID + peopleData.peopleid, peopleData.contactid);
	    editor.putString(FIELD_NAME + peopleData.peopleid, peopleData.name);
	    editor.putString(FIELD_NUMBER + peopleData.peopleid, peopleData.number);
	    editor.putInt(FIELD_COUNT + peopleData.peopleid, peopleData.count);
	    // Commit the edits!
	    editor.commit();
	    
	}

	public PeopleData getPeopleData(int id)
	{
		PeopleData peopleData = new PeopleData(id, 0, "", "", 0);
		peopleData.contactid = sharedprefs.getInt(FIELD_CONTACTID + id, 0);
		peopleData.name = sharedprefs.getString(FIELD_NAME + id, "");
		peopleData.number = sharedprefs.getString(FIELD_NUMBER + id, "");
		peopleData.count = sharedprefs.getInt(FIELD_COUNT + id, 0);
		return peopleData;
	}
	
	public boolean deletePeopleData(int id)
	{
		int max = getMaxItems();
		PeopleData peopleData;
		for(int i = id; i < max ; ++i)
		{
			if( (i + 1) <= max)
			{
				peopleData = getPeopleData(i+1);
				peopleData.peopleid = i;
				writePeopleData(peopleData);
				peopleData = null;
			}
		}
		
		peopleData = new PeopleData(max, 0, "", "", 0);
		writePeopleData(peopleData);
		peopleData = null;
		
		setMaxItems(max - 1);

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
	
	public boolean exists(PeopleData peopleData)
	{
		boolean exists = false;
		int max = getMaxItems();
		PeopleData peopleData1;
		for(int i = 1; i < max ; ++i)
		{
			peopleData1 = getPeopleData(i);
			if(peopleData1.name.equalsIgnoreCase(peopleData.name))
			{
				peopleData1 = null;
				exists = true;
				break;
			}
			peopleData1 = null;
		}
		return exists;
	}
}
