package com.example.secretmessage.pojo;

import android.content.Context;
import android.widget.Toast;

public class Contact {
	static final String TAG = Contact.class.getSimpleName();

	int _id;
	String _name;
	String _phone_number;
	String _message;
	
	public Contact() {}
	public Contact(int id, String name, String _phone_number, String message) {
		this._id = id;
		this._name = name;
		this._phone_number = _phone_number;
		this._message = message;
	}	
	public Contact(String name, String _phone_number, String message) {
		this._name = name;
		this._phone_number = _phone_number;
		this._message = message;
	}
	public Contact(String _phone_number, String message, Context context) {		
		this._phone_number = _phone_number;
		this._message = message;
		Toast.makeText(context, "Database created", Toast.LENGTH_LONG).show();
	}
		
	public int getID() {
		return this._id;
	}
	public void setID(int id) {
		this._id = id;
	}
	public String getName() {
		return this._name;
	}
	public void setName(String name) {
		this._name = name;	
	}
	public String getPhoneNumber() {
		return this._phone_number;
	}
	public void setPhoneNumber(String phone_number) {
		this._phone_number = phone_number;
	}
	public String getMessage() {
		return this._message;
	}
	public void setMessage(String message) {
		this._message = message;
	}
}

