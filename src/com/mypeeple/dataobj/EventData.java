package com.mypeeple.dataobj;

public class EventData {
	public int eventid;
	public int locationid;
	public String type;
	public String message;
	public int radius;
	
	public EventData(int eventid, int locationid, String type, String message, int radius)
	{
		this.eventid = eventid;
		this.locationid = locationid;
		this.type = type;
		this.message = message;
		this.radius = radius;
	}
}
