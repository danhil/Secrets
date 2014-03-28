package com.example.secretmessage.pojo;

import android.content.Context;
import android.widget.Toast;

public class Contact {
	static final String TAG = Contact.class.getSimpleName();

	String _phone_number;
	HandshakeStatus hs;
	
	public Contact() {}
	public Contact(String _phone_number) {
		this.hs = HandshakeStatus.INIT;
		this._phone_number = _phone_number;
	}	
	public Contact(String _phone_number, String hsStatus) {
		this.hs = HandshakeStatus.valueOf(hsStatus);
		this._phone_number = _phone_number;
	}	
	public String getPhoneNumber() {
		return this._phone_number;
	}
	
	public HandshakeStatus getHsStatus()
	{
		return this.hs;
	}
}

