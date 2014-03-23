package com.example.secretmessage.handler;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.secretmessage.utils.StringUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class ContactHandler
{
	static final String TAG = ContactHandler.class.getSimpleName();

	// Working variables
	public static HashMap<String, List<String>> namesAndNumbers;

	// Constructor
	public ContactHandler(Context context)
	{
		namesAndNumbers = new HashMap<String, List<String>>();
	};

	// Re-fetches contact names and numbers
	public void getContactsList(Context context)
	{	
		// Clear names and numbers list
		namesAndNumbers.clear();

		// Set up cursors
		ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		Cursor cc;

		// Working variables
		String id;
		String name;
		String curNumber;

		// Scroll through contacts until we find the one we need
		if (c.moveToFirst())
		{
			do
			{
				// Get ID and name
				id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
				name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
				{	
					// Make a new list of phone numbers for this contact
					List<String> theseNumbers = new ArrayList<String>();

					// Get the current phone number (contact may have multiple numbers), add it to list of numbers
					cc = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
							new String[]{id}, null);
					cc.moveToFirst();

					do
					{
						curNumber = cc.getString(cc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						curNumber = StringUtils.removeSpecialCharacters(curNumber);
						theseNumbers.add(curNumber);
					} while (cc.moveToNext());

					// Make a new hashmap entry for this contact
					namesAndNumbers.put(name, theseNumbers);

					// End, close cursor
					cc.close();
				}
			} while (c.moveToNext());

			// End, close cursor
			c.close();

		}
		else	// There were no contacts to check
		{
			c.close();
		}
	};

	// Returns the contact name of the given address
	public String getName(String address, Context context)
	{
		return getKeyByValue(namesAndNumbers, address, context);
	};

	// Returns the key to which the given value is mapped (generic)
	public String getKeyByValue(HashMap<String, List<String>> map, String value, Context context)
	{
		String thisNumber;
		int count = 0;
		for (Map.Entry<String, List<String>> entry : map.entrySet())
		{
			for (count = 0; count < entry.getValue().size(); count++)
			{
				thisNumber = entry.getValue().get(count);
				thisNumber = "1" + StringUtils.removeSpecialCharacters(thisNumber);
				if (thisNumber.equals(value)) return entry.getKey();
			}
		}
		return value;
	};

};

