package com.mypeeple.places;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ZoomButtonsController;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.mypeeple.R;
import com.mypeeple.utils.Constants;
import com.mypeeple.utils.Debug;
import com.mypeeple.dataobj.LocationData;
import com.mypeeple.db.SharedLocationDB;

public class PlacesActivity extends MapActivity {
	MapView mapView;
	GeoPoint geopoint;
	Activity currentactivity;
	MyLocationOverlay myLocationOverlay;

	class MapOverlay extends com.google.android.maps.Overlay {
		private long lastClickTime = -1;
		private long lastMouseDownTime = -1;

		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
				long when) {
			super.draw(canvas, mapView, shadow);

			if (geopoint == null)
				return false;

			// ---translate the GeoPoint to screen pixels---
			Point screenPts = new Point();
			mapView.getProjection().toPixels(geopoint, screenPts);

			// ---add the marker---
			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.marker);
			canvas.drawBitmap(bmp, screenPts.x - (bmp.getWidth() / 2), screenPts.y - bmp.getHeight(),
					null);
			return true;
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView1) {
			// ---when user lifts his finger---
			if (event.getAction() == MotionEvent.ACTION_UP) {
				long thisTime = System.currentTimeMillis();
				if (thisTime - lastMouseDownTime < 250) {
					lastMouseDownTime = -1;
					geopoint = null;
					geopoint = mapView1.getProjection().fromPixels(
							(int) event.getX(), (int) event.getY());
	
					mapView1.getController().animateTo(geopoint);
					//mapView1.getController().setZoom(17);
	
					mapView1.invalidate();
	
					Handler handler = new Handler();
					Runnable r=new Runnable()
					{
	
					    public void run() 
					    {
							Geocoder geoCoder = new Geocoder(getBaseContext(),
									Locale.getDefault());
							try {
								List<Address> addresses = geoCoder.getFromLocation(
										geopoint.getLatitudeE6() / 1E6,
										geopoint.getLongitudeE6() / 1E6, 1);
	
								String add = "";
								if (addresses.size() > 0) {
									for (int i = 0; i < addresses.get(0)
											.getMaxAddressLineIndex(); i++)
										add += addresses.get(0).getAddressLine(i) + "\n";
								}
	
								final TextView addressText = (TextView) findViewById(R.id.addressTextView);
								addressText.setText(add);
								
								//Toast.makeText(getBaseContext(), add, Toast.LENGTH_LONG)
										//.show();
							} catch (IOException e) {
								e.printStackTrace();
							}				    	
					                              
					    }
					};
					handler.post(r);
				}
				return false;
			} else if (event.getAction() == MotionEvent.ACTION_DOWN) {

				long thisTime = System.currentTimeMillis();
				if (thisTime - lastClickTime < 400) {

					// Double click
					mapView1.getController().zoomInFixing((int) event.getX(),
							(int) event.getY());
					lastClickTime = -1;
				} else {
					lastMouseDownTime = thisTime;
					lastClickTime = thisTime;
				}
				return false;
			} else
				return false;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		currentactivity = this;
		super.onCreate(savedInstanceState);
		if(Debug.DEBUG.equalsIgnoreCase("false"))
			setContentView(R.layout.maps);
		else
			setContentView(R.layout.maps_debug);

		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		ZoomButtonsController zbc = mapView.getZoomButtonsController();
		zbc.setVisible(true);
		zbc.setAutoDismissed(false);

		final ToggleButton togglebutton = (ToggleButton) findViewById(R.id.satelliteButton);
		togglebutton.setOnCheckedChangeListener(mSatelliteToggleButtonListener);

		final Button doneButton = (Button) findViewById(R.id.doneButton);
		doneButton.setOnTouchListener(mDoneButtonListener);

		final Button cancelButton = (Button) findViewById(R.id.cancelButton);
		cancelButton.setOnTouchListener(mCancelButtonListener);

		if (mapView.isSatellite())
			togglebutton.setChecked(true);
		else
			togglebutton.setChecked(false);

		// ---Add a location marker---
		MapOverlay mapOverlay = new MapOverlay();
		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();
		listOfOverlays.add(mapOverlay);

		int id = getIntent().getIntExtra(Constants.INTENT_EXTRAS_ID, 0);
		SharedLocationDB sld = new SharedLocationDB(this);
		LocationData locationData = sld.getLocationData(id);
		sld = null;

		geopoint = new GeoPoint(locationData.latitudeE6, locationData.longitudeE6);

		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		myLocationOverlay.enableMyLocation();
		if (locationData.initialized) {
			mapView.getController().animateTo(geopoint);
			mapView.getController().setZoom(17);
		} else {
			myLocationOverlay.runOnFirstFix(mRunOnFirstFix);
		}

		init(locationData.address);
		
		locationData = null;
		mapView.invalidate();
	}

	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	void init(String address) {
		String name = getIntent().getStringExtra(Constants.INTENT_EXTRAS_NAME);
		final EditText editText = (EditText) findViewById(R.id.placeText);
		
		if (!name.equalsIgnoreCase(this.getString(R.string.add_favorite)))
			editText.setText(name);
		
		final TextView textview = (TextView) findViewById(R.id.addressTextView);
		if(!address.equalsIgnoreCase(""))
			textview.setText(address);
	}

    private OnTouchListener mDoneButtonListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			// Perform action on clicks
			if (event.getAction() == MotionEvent.ACTION_UP) {
				// Done Button
				int id = getIntent().getIntExtra(
						Constants.INTENT_EXTRAS_ID, 0);
				final EditText editText = (EditText) findViewById(R.id.placeText);
				Editable editablename = editText.getText();
				String name = editablename.toString();

				final TextView addressText = (TextView) findViewById(R.id.addressTextView);
				CharSequence address = addressText.getText();

				if (!name.equalsIgnoreCase("")) {
					LocationData locationData = new LocationData(id, name, address.toString(), geopoint
							.getLatitudeE6(), geopoint.getLongitudeE6());
					SharedLocationDB sld = new SharedLocationDB(
							currentactivity);
					sld.writeLocationData(locationData, true, true);
					locationData = null;

					finish();
				} else {
					// AlertDialog
					AlertDialog.Builder builder = new AlertDialog.Builder(
							currentactivity);
					builder.setMessage(currentactivity
							.getString(R.string.enter_valid_name));
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
	
	private OnCheckedChangeListener mSatelliteToggleButtonListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// Perform action on clicks
			if (isChecked) {
				mapView.setSatellite(true);
			} else {
				mapView.setSatellite(false);
			}
		}
	};
	
	private Runnable mRunOnFirstFix = new Runnable() {
		public void run() {
			mapView.getController().animateTo(
					myLocationOverlay.getMyLocation());
			mapView.getController().setZoom(17);
		}
	};
}
