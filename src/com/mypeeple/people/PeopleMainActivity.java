package com.mypeeple.people;

import java.util.HashMap;
import java.util.Vector;

import com.mypeeple.R;
import com.mypeeple.dataobj.PeopleData;
import com.mypeeple.db.SharedPeopleDB;
import com.mypeeple.utils.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Contacts;
import android.telephony.PhoneNumberUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PeopleMainActivity extends ListActivity {
	static final int PICK_CONTACT = 100;
	ListActivity currentactivity;
	long deleteItemId;

	private class NumberCount {
		public NumberCount(int count) {
			this.count = count;
		}

		public int count;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		this.currentactivity = this;
		registerForContextMenu(getListView());
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position == 0) {
			Intent intent = new Intent(Intent.ACTION_PICK, Contacts.People.CONTENT_URI);
			startActivityForResult(intent, PICK_CONTACT);
		}
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					String name = c.getString(c
							.getColumnIndexOrThrow(Contacts.People.NAME));

					int id = c.getInt(c
							.getColumnIndexOrThrow(Contacts.People._ID));

					Uri personUri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI,
							id);
					Uri phonesUri = Uri.withAppendedPath(personUri,
							Contacts.People.Phones.CONTENT_DIRECTORY);
					String[] proj = new String[] { Contacts.Phones._ID, Contacts.Phones.TYPE,
							Contacts.Phones.NUMBER, Contacts.Phones.LABEL };
					Cursor peoplecursor = getContentResolver().query(phonesUri,
							proj, null, null, null);

					PeopleData pd = new PeopleData(0, id, name, "", 1);
					if (peoplecursor.moveToFirst()) {
						do {
							String phoneNumber = peoplecursor
									.getString(peoplecursor
											.getColumnIndex(Contacts.Phones.NUMBER));
							String strip = PhoneNumberUtils
									.stripSeparators(phoneNumber);
							if(!strip.equals(""))
							{
								pd.number = strip;
								break;
							}
						} while (peoplecursor.moveToNext());
					}
					SharedPeopleDB spd = new SharedPeopleDB(this);
					if(!spd.exists(pd))
						spd.writePeopleData(pd);
				}
			}
			break;
		}
	}

	public void onResume() {
		super.onResume();
		ProgressDialog dialog = ProgressDialog.show(this, "",
				this.getString(R.string.please_wait), true);

		init();

		dialog.dismiss();
	}

	void init() {
		String[] peopleString = getPeople();
		this.setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, peopleString));
		this.getListView().setTextFilterEnabled(true);
	}

	String[] getPeople() {
		SharedPeopleDB spd = new SharedPeopleDB(this);
		int max = spd.getMaxItems();
		if (max == 0) {
			suggestPeople();
			max = spd.getMaxItems();
		}

		String[] mStrings = new String[max + 1];
		mStrings[0] = this.getString(R.string.add_people);
		PeopleData pd;
		for (int i = 1; i <= max; ++i) {
			pd = spd.getPeopleData(i);
			mStrings[i] = pd.name;
			pd = null;
		}
		return mStrings;
	}

	void suggestPeople() {
		HashMap<String, NumberCount> calllogNumbers = new HashMap<String, NumberCount>();
		Vector<PeopleData> suggestedPeople = new Vector<PeopleData>();

		// Querying for a cursor is like querying for any SQL-Database
		Cursor c = getContentResolver().query(
				CallLog.Calls.CONTENT_URI, null, null, null,
				CallLog.Calls.DATE + " DESC");
		
		int numberColumn = 0;

		if (c != null)
		{
			startManagingCursor(c);
	
			// Retrieve the column-indixes of phoneNumber, date and calltype
			numberColumn = c
					.getColumnIndex(CallLog.Calls.NUMBER);
	
			// Loop through all entries the cursor provides to us.
			if (c.moveToFirst()) {
				do {
					String callerPhoneNumber = c.getString(numberColumn);
	
					NumberCount i = calllogNumbers.get(callerPhoneNumber);
					if (i == null) {
						NumberCount num = new NumberCount(1);
						calllogNumbers.put(callerPhoneNumber, num);
					} else {
						i.count++;
					}
				} while (c.moveToNext());
			}
			c.close();
		}

		String[] projection = new String[] { Contacts.People._ID,
				Contacts.People.NAME, Contacts.People.NUMBER,
				Contacts.People.TYPE };

		Cursor contactscursor = getContentResolver().query(
				Contacts.People.CONTENT_URI, projection, null, null,
				Contacts.People.NAME + " ASC");
		startManagingCursor(contactscursor);

		// Retrieve the column-indixes of phoneNumber, date and calltype
		int idColumn = contactscursor.getColumnIndex(Contacts.People._ID);
		numberColumn = contactscursor.getColumnIndex(Contacts.People.NUMBER);
		int nameColumn = contactscursor.getColumnIndex(Contacts.People.NAME);

		if (contactscursor != null)
		{
			if (contactscursor.moveToFirst()) {
				do {
					//String callerPhoneNumber = contactscursor
						//	.getString(numberColumn);
					String name = contactscursor.getString(nameColumn);
					int id = contactscursor.getInt(idColumn);
	
					Uri personUri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI,
							id);
					Uri phonesUri = Uri.withAppendedPath(personUri,
							Contacts.People.Phones.CONTENT_DIRECTORY);
					String[] proj = new String[] { Contacts.Phones._ID, Contacts.Phones.TYPE,
							Contacts.Phones.NUMBER, Contacts.Phones.LABEL };
					Cursor peoplecursor = getContentResolver().query(phonesUri,
							proj, null, null, null);
	
					if (peoplecursor != null)
					{
						if (peoplecursor.moveToFirst()) {
							do {
								String phoneNumber = peoplecursor
										.getString(peoplecursor
												.getColumnIndex(Contacts.Phones.NUMBER));
								String strip = PhoneNumberUtils
										.stripSeparators(phoneNumber);
								if (calllogNumbers.get(strip) != null) {
									PeopleData pd = new PeopleData(
											0,
											id,
											name,
											strip,
											((NumberCount) calllogNumbers.get(strip)).count);
									if (suggestedPeople.size() < 5) {
										suggestedPeople.add(pd);
									} else if (suggestedPeople.size() == 5) {
										int min_index = 0;
										int min_count = 0;
										for (int i = 0; i < 5; i++) {
											PeopleData pd1 = suggestedPeople
													.elementAt(i);
											if (min_count == 0) {
												min_index = i;
												min_count = pd1.count;
											}
		
											if (pd1.count < min_count) {
												min_index = i;
												min_count = pd1.count;
											}
										}
										suggestedPeople.setElementAt(pd, min_index);
									}
		
								}
							} while (peoplecursor.moveToNext());
						}
						peoplecursor.close();
					}
				} while (contactscursor.moveToNext());
			}
			contactscursor.close();
		}

		SharedPeopleDB spd = new SharedPeopleDB(this);
		for (int i = 0; i < suggestedPeople.size(); ++i) {
			spd.writePeopleData(suggestedPeople.elementAt(i));
		}
		spd = null;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			// Log.e(TAG, "bad menuInfo", e);
			return;
		}
		long id = getListAdapter().getItemId(info.position);

		if (id != 0) {
			menu.setHeaderTitle(R.string.delete_people);
			menu.add(0, Constants.MENU_ITEM_DELETE, 0, R.string.delete);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			// Log.e(TAG, "bad menuInfo", e);
			return false;
		}

		if (item.getItemId() == Constants.MENU_ITEM_DELETE) {
			deleteItemId = getListAdapter().getItemId(info.position);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.delete_people);
			builder.setMessage(this.getString(R.string.delete_people_confirm)
					+ " \""
					+ (String) currentactivity.getListAdapter().getItem(
							info.position) + "\"?");
			builder.setCancelable(false);
			builder.setPositiveButton(this.getString(R.string.yes), mOnAlertDialogPositiveButton);
			builder.setNegativeButton(this.getString(R.string.no), mOnAlertDialogNegativeButton);
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
			SharedPeopleDB spd = new SharedPeopleDB(currentactivity);
			spd.deletePeopleData((int) deleteItemId);
			init();
		}
	};
	
	private DialogInterface.OnClickListener mOnAlertDialogNegativeButton = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog,
				int id) {

		}
	};
}
