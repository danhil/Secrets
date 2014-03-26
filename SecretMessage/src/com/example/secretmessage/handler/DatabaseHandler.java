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

public class DatabaseHandler extends SQLiteOpenHelper {
	static final String TAG = DatabaseHandler.class.getSimpleName();

	private static final int DATABASE_VERSION = 1090;
	private static final String DATABASE_NAME = "contactsManagerNew.db";
	public static final String TABLE_CONTACTS = "contacts";
	public static final String KEY_ID = "id";
	public static final String KEY_NAME = "name";
	public static final String KEY_PH_NO = "phone_number";
	public static final String KEY_MSG = "Message";
		
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);		
	}
	
	
	public void onCreate(SQLiteDatabase db) {		
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
				+ KEY_PH_NO + " TEXT," + KEY_MSG + " VARCHAR " +")";
		db.execSQL(CREATE_CONTACTS_TABLE);
	}
		
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
		onCreate(db);
	}
	
	// All Create, Update, Delete Operations
	
	void addContact(Contact contact) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, contact.getName());
		values.put(KEY_PH_NO, contact.getPhoneNumber());
		values.put(KEY_MSG, contact.getMessage());
		
		db.insert(TABLE_CONTACTS, null, values);
		db.close();	
	}
	
	
	Contact getContact(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID, KEY_NAME, KEY_PH_NO, KEY_MSG }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null,null);
		if(cursor !=null)
			cursor.moveToFirst();
		
		Contact contact = new Contact(Integer.parseInt(cursor.getString(0)),
				cursor.getString(1), cursor.getString(2), cursor.getString(3));
		return contact;
	}
	
	public List<Contact> getContacts() {
		List<Contact> contactList = new ArrayList<Contact>();
		String selectQuery = "SELECT * FROM " + TABLE_CONTACTS;
		
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				Contact contact = new Contact();
				contact.setID(Integer.parseInt(cursor.getString(0)));
				contact.setName(cursor.getString(1));
				contact.setPhoneNumber(cursor.getString(2));
				contact.setMessage(cursor.getString(3));
				contactList.add(contact);
			} while (cursor.moveToNext());			
		}
		
		return contactList;
	}	
}

