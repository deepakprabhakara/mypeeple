package com.mypeeple.home;

import java.util.Date;

import com.mypeeple.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.mypeeple.dataobj.NotificationData;
import com.mypeeple.db.SharedNotificationDB;
import com.mypeeple.utils.Constants;

public class HomeMainActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
	}
	
	public void onResume()
	{
		super.onResume();
		
		init(); 
	}
	
	public void onStart()
	{
		super.onStart();
		//init();
	}

	void init()
	{
		final TextView textView = (TextView) findViewById(R.id.homeTextView);
		String s = populateNotifications();
		textView.setText(s);
	}

	String populateNotifications()
	{
		StringBuilder text = new StringBuilder();
		SharedNotificationDB snd = new SharedNotificationDB(this);
		
		NotificationData notificationData;
		for(int i = 1; i <= snd.getMaxItems(); ++i)
		{
			notificationData = snd.getNotificationData(i);
			text.append("You ");
			if(notificationData.type.equalsIgnoreCase(Constants.NOTIFICATION_ENTER))
				text.append("entered ");
			else
				text.append("left ");
			text.append("\"" + notificationData.locationname + "\"");
			text.append(" on ");
			Date date = new Date(notificationData.timestamp);
			text.append(date.toLocaleString() + "\n\n");
			notificationData = null;
		}
		
		return text.toString();
	}
}
