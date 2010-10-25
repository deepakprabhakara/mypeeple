package com.mypeeple.places;

import com.mypeeple.R;
import com.mypeeple.utils.Constants;
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
 
public class PlacesMainActivity extends ListActivity {
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
        String name = (String) l.getItemAtPosition(position);
		
		Intent myIntent = new Intent(this, PlacesActivity.class);
		myIntent.putExtra(Constants.INTENT_EXTRAS_NAME, name);
		myIntent.putExtra(Constants.INTENT_EXTRAS_ID, position);
		this.startActivity(myIntent);
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
    	SharedLocationDB sld = new SharedLocationDB(this);
        String[] mStrings = sld.getLocationsString(true);
        sld = null;

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
			builder.setTitle(R.string.delete_location);
			builder.setMessage(this
					.getString(R.string.delete_location_confirm) + " \"" + (String)currentactivity.getListAdapter().getItem(info.position) + "\"?");
			builder.setCancelable(false);
			builder.setPositiveButton(
					this.getString(R.string.yes), mOnAlertDialogPositiveButton);

			builder.setNegativeButton(
					this.getString(R.string.no), mOnAlertDialogNegativeButton);
			
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
    		SharedLocationDB sld = new SharedLocationDB(currentactivity);
    		sld.deleteLocationData((int)deleteItemId);
    		init();
		}
	};
	
	private DialogInterface.OnClickListener mOnAlertDialogNegativeButton = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog,
				int id) {

		}
	};
}
