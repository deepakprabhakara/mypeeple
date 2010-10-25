package com.mypeeple.events;

import com.mypeeple.R;
import com.mypeeple.dataobj.EventData;
import com.mypeeple.db.SharedEventsDB;
import com.mypeeple.db.SharedLocationDB;
import com.mypeeple.utils.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
 
public class EventsActivity extends Activity {
	Activity currentactivity;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.currentactivity = this;
        setContentView(R.layout.event_screen);

        // Create an ArrayAdapter, that will populate the Type (Enter/Leave) Strings into the DropDown menu
        Spinner s = (Spinner) findViewById(R.id.typeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.event_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

        // Create an ArrayAdapter, that will populate the Location Strings into the DropDown menu
    	SharedLocationDB sld = new SharedLocationDB(this);
        String[] mStrings = sld.getLocationsString(false);
        sld = null;

        ArrayAdapter<String> locationadapter = new ArrayAdapter<String>(this,
                                        android.R.layout.simple_spinner_item, mStrings);
        locationadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner placesspinner = (Spinner) findViewById(R.id.placeSpinner);
        placesspinner.setAdapter(locationadapter);

        // Done Button
		final Button doneButton = (Button) findViewById(R.id.doneButton);
		doneButton.setOnTouchListener(mDoneButtonListener);

        // Cancel Button
		final Button cancelButton = (Button) findViewById(R.id.cancelButton);
		cancelButton.setOnTouchListener(mCancelButtonListener);

		init();
    }
    
    void init()
    {
		int id = getIntent().getIntExtra(
				Constants.INTENT_EXTRAS_ID, 0);
		SharedEventsDB sed = new SharedEventsDB(this);
		EventData eventData = sed.getEventData(id);
		
		final EditText editText = (EditText) findViewById(R.id.eventText);
		editText.setText(eventData.message);
		if (eventData.message.equalsIgnoreCase(this.getString(R.string.add_new_event)))
			editText.setSelectAllOnFocus(true);

		Spinner s = (Spinner) findViewById(R.id.typeSpinner);
		if(eventData.type.equalsIgnoreCase(Constants.NOTIFICATION_LEAVE))
			s.setSelection(1);
		
		Spinner placesspinner = (Spinner) findViewById(R.id.placeSpinner);
		if(eventData.locationid >= 1)
			placesspinner.setSelection(eventData.locationid - 1);
    }
    
    private OnTouchListener mDoneButtonListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			// Perform action on clicks
			if (event.getAction() == MotionEvent.ACTION_UP) {
				// Done Button
				int id = getIntent().getIntExtra(
						Constants.INTENT_EXTRAS_ID, 0);
				final EditText editText = (EditText) findViewById(R.id.eventText);
				Editable editablename = editText.getText();
				String eventText = editablename.toString();

				if (!eventText.equalsIgnoreCase("")) {

					Spinner s = (Spinner) findViewById(R.id.typeSpinner);
					
					String type = Constants.NOTIFICATION_LEAVE;
					if(s.getSelectedItemPosition() == 0)
						type = Constants.NOTIFICATION_ENTER;
					
					Spinner placeSpinner = (Spinner) findViewById(R.id.placeSpinner);
					
					int locationid = placeSpinner.getSelectedItemPosition() + 1;
					
					EventData eventData = new EventData(id, locationid, type, eventText, (int) Constants.DISTANCE_THRESHOLD);
					SharedEventsDB sed = new SharedEventsDB(currentactivity);
					sed.writeEventData(eventData, true);
					eventData = null;
					
					finish();
				} else {
					// AlertDialog
					AlertDialog.Builder builder = new AlertDialog.Builder(
							currentactivity);
					builder.setMessage(currentactivity
							.getString(R.string.enter_valid_event));
					builder.setCancelable(false);
					builder.setPositiveButton(
							currentactivity.getString(R.string.done),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

								}
							});
					AlertDialog alert = builder.create();
					alert.show();
					alert = null;
					builder = null;
				}
			}

			return false;
		}
	};
	
	private OnTouchListener mCancelButtonListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			// Perform action on clicks
			if (event.getAction() == MotionEvent.ACTION_UP) {
				// Cancel Button
				finish();
			}

			return false;
		}
	};
}
