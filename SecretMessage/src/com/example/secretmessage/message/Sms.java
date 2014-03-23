package com.example.secretmessage.message;

import com.example.secretmessage.handler.EncryptionHandler;

import android.util.Base64;

public class Sms
{
	static final String TAG = Sms.class.getSimpleName();

	public String text;
	public char headerValue;
	public int[] saltIndex;
	public static EncryptionHandler encrypt = null;
	public byte[] key;
	public boolean isHandshake = false;
	public String recipient;
	
	public Sms(String Text, String password, String Recipient)
	{
		text = Text;
		encrypt = EncryptionHandler.getInstance(password);
		isHandshake = false;
		recipient = Recipient;
		saltIndex = new int[1];
	}
	
	public Sms(char HeaderValue, byte[] Key, String Password, String Recipient)
	{
		headerValue = HeaderValue;
		key = Key;
		encrypt = EncryptionHandler.getInstance(Password);
		recipient = Recipient;
		isHandshake = true;
		saltIndex = new int[1];
	}
	
	public String serialize(String Password)
	{
		try {
			if (isHandshake)
			{
				byte[] temp = {(byte) headerValue};
				temp = concatenate(temp, key);
				return "" + (char) 28 + Base64.encodeToString(temp, Base64.DEFAULT);
			}
			else
			{
				byte[] temp = {0xF};
				temp = concatenate(temp, text.getBytes("UTF-8"));
				temp = encrypt.encrypt(temp, recipient, saltIndex, Password);
				return "" + (char) 29 + (char) saltIndex[0] + Base64.encodeToString(temp, Base64.DEFAULT);
			}
		} catch (Exception e) { return null; }
	}
	
	private byte[] concatenate(byte[] a, byte[] b)
	{
		byte rtrn[] = new byte[a.length + b.length];
		int i = 0;
		for (; i < a.length; i++)
			rtrn[i] = a[i];
		for (int j = 0; j < b.length; j++)
			rtrn[i + j] = b[j];
		return rtrn;
	}
	
}