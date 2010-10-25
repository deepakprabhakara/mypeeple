package com.mypeeple.events;

import com.mypeeple.R;
import com.mypeeple.utils.Constants;
import com.mypeeple.dataobj.EventData;
import com.mypeeple.db.SharedEventsDB;
import com.mypeeple.db.SharedLocationDB;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
 
public class EventsMainActivity extends ListActivity {
	ListActivity currentactivity;
	long deleteItemId;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.currentactivity = this;
        registerForContextMenu(getListView());
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {	
		Intent myIntent = new Intent(this, EventsActivity.class);
		myIntent.putExtra(Constants.INTENT_EXTRAS_ID, position);
		this.startActivity(myIntent);
    }
    
    String[] getEvents()
    {
    	SharedEventsDB sed = new SharedEventsDB(this);
    	int max = sed.getMaxItems();
    	String[] mStrings = new String[max+1]; // Add New Event
    	mStrings[0] = this.getString(R.string.add_new_event);
    	EventData eventData;

    	SharedLocationDB sld = new SharedLocationDB(this);
        String[] mLocationStrings = sld.getLocationsString(false);
        sld = null;

    	for(int i = 1; i <= max; ++i)
    	{
    		eventData = sed.getEventData(i);
    		
    		mStrings[i] = eventData.message;
    		mStrings[i] += " " + getString(R.string.type_label) + " ";
    		mStrings[i] += eventData.type.equalsIgnoreCase(Constants.NOTIFICATION_ENTER) ? getString(R.string.enter) : getString(R.string.leave);
    		if(eventData.locationid - 1 < mLocationStrings.length)
    			mStrings[i] += " " + mLocationStrings[eventData.locationid - 1]; // map locationid to name here
    		
    		eventData = null;
    	}
    	return mStrings;
    }

    public void onStart()
    {
    	super.onStart();
    }

    public void onResume()
    {
    	super.onResume();
    	init();
    }
    
    void init()
    {
        // Create an array of Strings, that will be put to our ListActivity
        String[] mStrings = getEvents();
 
        // Create an ArrayAdapter, that will actually make the Strings above appear in the ListView
        this.setListAdapter(new ArrayAdapter<String>(this,
                                        android.R.layout.simple_list_item_1, mStrings));
        this.getListView().setTextFilterEnabled(true);
    }
    
    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
    	AdapterView.AdapterContextMenuInfo info;
    	try {
    	    info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    	} catch (ClassCastException e) {
    	    //Log.e(TAG, "bad menuInfo", e);
    	    return;
    	}
    	long id = getListAdapter().getItemId(info.position);
    	
    	if(id != 0)
    	{
    		menu.setHeaderTitle(R.string.delete_location);
    		menu.add(0, Constants.MENU_ITEM_DELETE, 0, R.string.delete);
    	}
    }
    
    @Override
    public boolean onMenuItemSelected (int featureId, MenuItem item)
    {
    	AdapterView.AdapterContextMenuInfo info;
    	try {
    	    info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    	} catch (ClassCastException e) {
    	    //Log.e(TAG, "bad menuInfo", e);
    	    return false;
    	}

    	if(item.getItemId() == Constants.MENU_ITEM_DELETE)
    	{
        	deleteItemId = getListAdapter().getItemId(info.position);
        	
			AlertDialog.Builder builder = new AlertDialog.Builder(
					this);
			builder.setTitle(R.string.delete_event);
			builder.setMessage(this
					.getString(R.string.delete_event_confirm) + " \"" + (String)currentactivity.getListAdapter().getItem(info.position) + "\"?");
			builder.setCancelable(false);
			builder.setPositiveButton(
					this.getString(R.string.yes), mOnAlertDialogPositiveButton);
			builder.setNegativeButton(
					this.getString(R.string.no),mOnAlertDialogNegativeButton);

			AlertDialog alert = builder.create();
			alert.show();
			alert = null;
			builder = null;
			return true;
    	}
    	
    	return super.onMenuItemSelected(featureId, item);
    }
    
	private DialogInterface.OnClickListener mOnAlertDialogPositiveButton = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog,
				int id) {
    		SharedEventsDB sed = new SharedEventsDB(currentactivity);
    		sed.deleteEventData((int)deleteItemId);
    		init();
		}
	};
	
	private DialogInterface.OnClickListener mOnAlertDialogNegativeButton = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog,
				int id) {

		}
	};
}
