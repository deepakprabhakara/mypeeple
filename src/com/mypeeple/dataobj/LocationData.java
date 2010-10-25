package com.mypeeple.dataobj;

public class LocationData {
	public int locationid;
	public String name;
	public String address;
	public int latitudeE6;
	public int longitudeE6;
	public float distance; // distance from a given location
	public boolean initialized;
	
	public LocationData(int locationid, String name, String address, int latitude, int longitude)
	{
		this.locationid = locationid;
		this.name = name;
		this.address = address;
		this.latitudeE6 = latitude;
		this.longitudeE6 = longitude;
		this.distance = -1;
		this.initialized = false;
	}
}
