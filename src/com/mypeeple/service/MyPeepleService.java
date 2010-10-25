package com.mypeeple.service;

import java.util.HashMap;
import java.util.Vector;

import com.mypeeple.MyPeepleActivity;
import com.mypeeple.R;
import com.mypeeple.utils.Constants;
import com.mypeeple.dataobj.EventData;
import com.mypeeple.dataobj.LocationData;
import com.mypeeple.dataobj.NotificationData;
import com.mypeeple.db.SharedEventsDB;
import com.mypeeple.db.SharedLocationDB;
import com.mypeeple.db.SharedNotificationDB;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MyPeepleService extends Service {

	private boolean started = false;
	private boolean locationChanged = false;
	private boolean eventChanged = false;
	private LocationManager locationManager;
    private Location currentLocation;
    private Service myService;
    private MyLocationListener locationListener;
    private Vector<LocationData> locationVector;
    private HashMap<String, Vector<EventData>> eventHashMap;
    
    private class MyLocationListener implements LocationListener
    {

		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			if(location == null)
				return;

        	currentLocation = location;
        	
			if(locationChanged)
			{
				locationVector = null;
				locationVector = populateLocationVector();
				locationChanged = false;
			}

			if(locationVector == null)
				return;

			if(eventChanged)
			{
				eventHashMap = null;
				eventHashMap = populateEventHashMap();
				eventChanged = false;
			}

			if(eventHashMap == null)
				return;
        	
        	for(int i = 0; i < locationVector.size(); ++i)
        	{
            	LocationData locationData = (LocationData)locationVector.get(i);
            	float[] results = new float[1];
            	Location.distanceBetween(location.getLatitude(), location.getLongitude(), locationData.latitudeE6 / 1E6, locationData.longitudeE6 / 1E6, results);
            	if(locationData.distance < Constants.DISTANCE_THRESHOLD && locationData.distance > 0)
            	{
            		if(results[0] > Constants.DISTANCE_THRESHOLD)
            		{
            			// We have left a Location
            			NotificationData notificationData = new NotificationData(0, locationData.locationid, locationData.name, 0, Constants.NOTIFICATION_LEAVE, "", System.currentTimeMillis());
            			SharedNotificationDB snd = new SharedNotificationDB(myService);
            			snd.writeNotificationData(notificationData);
            			notificationData = null;
            			
            			checkAndSendEventNotification(locationData, Constants.NOTIFICATION_LEAVE);
            			
            			Toast.makeText(myService, "You have left Location : " + locationData.name, Toast.LENGTH_LONG).show();
            		}
            	}
            	else if(locationData.distance > Constants.DISTANCE_THRESHOLD)
            	{
            		if(results[0] < Constants.DISTANCE_THRESHOLD)
            		{
            			// We have entered a Location
            			NotificationData notificationData = new NotificationData(0, locationData.locationid, locationData.name, 0, Constants.NOTIFICATION_ENTER, "", System.currentTimeMillis());
            			SharedNotificationDB snd = new SharedNotificationDB(myService);
            			snd.writeNotificationData(notificationData);
            			notificationData = null;
            			
            			checkAndSendEventNotification(locationData, Constants.NOTIFICATION_ENTER);
            			
            			Toast.makeText(myService, "You have entered Location : " + locationData.name, Toast.LENGTH_LONG).show();
            		}
            	}
            		
            	locationData.distance = results[0];
           		
           		Log.d("\n\nLOCATION ", "name=" + locationData.name + "distance=" + locationData.distance + "\n\n");
        	}
        	Toast.makeText(myService, "Location Update : " + location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_SHORT).show();			
		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		Log.d("MyPeepleService", "onBind");

		//Toast.makeText(this, "Service Binded", Toast.LENGTH_LONG).show();
		return null;

	}

	@Override
	public void onCreate() {
		Log.d("MyPeepleService", "onCreate");
		super.onCreate();
		myService = this;
		
		//Toast.makeText(this, "MyPeeple Service Created", Toast.LENGTH_LONG).show();

	}

	@Override
	public void onDestroy() {
		Log.d("MyPeepleService", "onDestroy");
		super.onDestroy();

		started = false;

		this.locationManager.removeUpdates(locationListener);
		Toast.makeText(this, "MyPeeple Service Closed", Toast.LENGTH_SHORT).show();
	}

	// This is the old onStart method that will be called on the pre-2.0
	// platform. On 2.0 or later we override onStartCommand() so this
	// method will not be called.
	@Override
	public void onStart(Intent intent, int startId) {
		handleCommand(intent, startId);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent, startId);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return 1;
	}

	public void handleCommand(Intent intent, int startId) {
		
		boolean locationchanged = false;
		if(intent != null)
			locationchanged = intent.getBooleanExtra(Constants.INTENT_EXTRAS_LOCATIONCHANGED, false);
		boolean eventchanged = false;
		if(intent != null)
			eventchanged = intent.getBooleanExtra(Constants.INTENT_EXTRAS_EVENTCHANGED, false);
		if(locationchanged)
		{
			locationChanged();
			return;
		}
		if(eventchanged)
		{
			eventChanged();
			return;
		}
		
		if (started)
			return;

		started = true;

		init();
		
		Log.d("MyPeepleService", "onStart");
		super.onStart(intent, startId);

		Toast.makeText(this, "MyPeeple Service Started", Toast.LENGTH_SHORT).show();
	}
	
	void init()
	{
		locationVector = null;
		locationVector = populateLocationVector();
		
		eventHashMap = null;
		eventHashMap = populateEventHashMap();
		
		// Get an instance of the android system LocationManager 
	    // so we can access the phone's GPS receiver
	    this.locationManager = 
	        (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    
	    // Subscribe to the location manager's updates on the current location
	    this.locationListener = new MyLocationListener();
	    this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long)60000, Constants.DISTANCE_THRESHOLD, this.locationListener);
	    //this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, (long)60000, Constants.DISTANCE_THRESHOLD, this.locationListener);
	}
	
	Vector<LocationData> populateLocationVector()
	{
		Vector<LocationData> vectorLocationData = new Vector<LocationData>();
		
    	SharedLocationDB sld = new SharedLocationDB(this);
    	int max = sld.getMaxItems();
    	for(int i = 1; i <= max; ++i)
    	{
			LocationData locationData = sld.getLocationData(i);
    		if(currentLocation != null && locationData.initialized)
    		{
            	float[] results = new float[1];
            	Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), locationData.latitudeE6 / 1E6, locationData.longitudeE6 / 1E6, results);
            	locationData.distance = results[0];
    		}
    		vectorLocationData.add(locationData);
    	}
    	
    	//if(vectorLocationData.size() != 0)
    	return vectorLocationData;
    	//else
    		//return null;
	}
	
	HashMap<String, Vector<EventData>> populateEventHashMap()
	{
		HashMap<String, Vector<EventData>> hashmap = new HashMap<String, Vector<EventData>>();

    	SharedEventsDB sed = new SharedEventsDB(this);
    	int max = sed.getMaxItems();
    	for(int i = 1; i <= max; ++i)
    	{
			EventData eventData = sed.getEventData(i);
			String key =  eventData.type + eventData.locationid;
			if(hashmap.containsKey(key))
			{
				// fetch vector and add EventData
				Vector<EventData> vectorEventData = hashmap.get(key);
				vectorEventData.add(eventData);
			}
			else
			{
				Vector<EventData> vectorEventData = new Vector<EventData>();
				vectorEventData.add(eventData);
				hashmap.put(key, vectorEventData);
			}
    	}
		return hashmap;
	}
	
	void locationChanged()
	{
		locationChanged = true;
	}

	void eventChanged()
	{
		eventChanged = true;
	}
	
	void checkAndSendEventNotification(LocationData ld, String type)
	{
		// check if hashmap has key <type + location id>
		if(eventHashMap == null)
			return;
		
		String key = type + ld.locationid;
		if(eventHashMap.containsKey(key))
		{
			Vector<EventData> vectorEventData = eventHashMap.get(key);
			for(int i = 0; i< vectorEventData.size(); ++i)
			{
				EventData ed = vectorEventData.elementAt(i);
				sendNotification(ed);
			}
		}
	}
	
	void sendNotification(EventData ed)
	{
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		
		int icon = R.drawable.icon;
		CharSequence tickerText = ed.message;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		Context context = getApplicationContext();
		CharSequence contentTitle = getString(R.string.app_name);
		CharSequence contentText = ed.message;
		Intent notificationIntent = new Intent(this, MyPeepleActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(ed.eventid, notification);
	}
}
