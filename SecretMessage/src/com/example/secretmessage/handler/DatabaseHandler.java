package com.example.secretmessage.handler;

import java.util.ArrayList;
import java.util.List;

import com.example.secretmessage.pojo.Contact;
import com.example.secretmessage.pojo.HandshakeStatus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {
	static final String TAG = DatabaseHandler.class.getSimpleName();

	private static final int DATABASE_VERSION = 1090;
	private static final String DATABASE_NAME = "contactsManager.db";
	public static final String TABLE_CONTACTS = "contacts";
	public static final String KEY_PH_NO = "phone_number";
	public static final String KEY_HSSTATUS = "hs_status";
		
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);		
	}
	
	
	public void onCreate(SQLiteDatabase db) {		
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
				+ KEY_PH_NO + " TEXT PRIMARY KEY," + KEY_HSSTATUS + " VARCHAR "+ ")";
		db.execSQL(CREATE_CONTACTS_TABLE);
	}
	
		
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
		onCreate(db);
	}
	
	// All Create, Update, Delete Operations
	
	void addContact(Contact contact) {
		Log.d(TAG, "Writing " + contact.getPhoneNumber() 
				+ contact.getHsStatus() + "to database");
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_PH_NO, contact.getPhoneNumber());
		values.put(KEY_HSSTATUS, contact.getHsStatus().getValue());
		db.insert(TABLE_CONTACTS, null, values);
		db.close();	
	}
	
	public int updateHsStatus(String phoneNbr, HandshakeStatus status)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(KEY_HSSTATUS, status.getValue());
		int affectedRows = db.update(TABLE_CONTACTS, cv, KEY_PH_NO+"="+phoneNbr, null);
		return affectedRows;
	}
	
	public HandshakeStatus getHSStatus(String phoneNbr)
	{
		Log.d(TAG, "Getting hs status for " + phoneNbr);
		SQLiteDatabase db = this.getWritableDatabase();
		String[] selectionArgs = new String[]{KEY_PH_NO, phoneNbr};
		Cursor cursor = db.query(TABLE_CONTACTS, 
				new String[]{KEY_HSSTATUS} ,KEY_PH_NO+"="+phoneNbr,
				null, null, null, null);
		String hsStatus = "";
		if(cursor.getCount() > 1)
			Log.e(TAG, "The cursor returned more than one hit...");
		if(cursor.moveToFirst()){
			hsStatus = cursor.getString(0);
		} else
		{
			addContact(new Contact(phoneNbr));
			// Redo the query to find the inserted contact.
			return getHSStatus(phoneNbr);
		}
		Log.d(TAG, "returning " + hsStatus);
		return HandshakeStatus.getStatus(hsStatus);
	}
	
	
	Contact getContact(String id) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.query(TABLE_CONTACTS, new String[] {KEY_PH_NO, KEY_HSSTATUS}, KEY_PH_NO + "=?",
				new String[] { id }, null, null, null,null);
		if(cursor !=null)
			cursor.moveToFirst();
		
		Contact contact = new Contact(cursor.getString(0),
				cursor.getString(1));
		return contact;
	}
	
	public List<Contact> getContacts() {
		List<Contact> contactList = new ArrayList<Contact>();
		String selectQuery = "SELECT * FROM " + TABLE_CONTACTS;
		
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				Contact contact = new Contact(cursor.getString(0), cursor.getString(1) );
				contactList.add(contact);
			} while (cursor.moveToNext());			
		}
		
		return contactList;
	}	
}

