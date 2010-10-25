package com.mypeeple.dataobj;

public class PeopleData {
	public int peopleid;
	public int contactid;
	public String name;
	public String number;
	public int count;
	
	public PeopleData(int peopleid, int contactid, String name, String number, int count)
	{
		this.peopleid = peopleid;
		this.contactid = contactid;
		this.name = name;
		this.number = number;
		this.count = count;
	}
}
