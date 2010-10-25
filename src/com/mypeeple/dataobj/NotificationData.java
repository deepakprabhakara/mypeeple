package com.mypeeple.dataobj;

public class NotificationData {
	public int notificationid;
	public int locationid;
	public String locationname;
	public int peopleid;
	public String type;
	public String additionalmessage;
	public long timestamp;
	
	public NotificationData(int notificationid, int locationid, String locationname, int peopleid, String type, String additionalmessage, long timestamp)
	{
		this.notificationid = notificationid;
		this.locationid = locationid;
		this.locationname = locationname;
		this.peopleid = peopleid;
		this.type = type;
		this.additionalmessage = additionalmessage;
		this.timestamp = timestamp;
	}
}
