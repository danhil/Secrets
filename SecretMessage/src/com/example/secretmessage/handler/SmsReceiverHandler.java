package com.example.secretmessage.handler;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiverHandler extends BroadcastReceiver
{
	static final String TAG = SmsReceiverHandler.class.getSimpleName();

	// Working variables
	public static final String BUNDLE_PDU_KEY = "pdus";
	public static final String MESAGE_URI = "content://sms";
	public static final String ADDRESS = "address";
    public static final String PERSON = "person";
    public static final String DATE = "date";
    public static final String READ = "read";
    public static final String STATUS = "status";
    public static final String TYPE = "type";
    public static final String BODY = "body";
    public static final String SEEN = "seen";
    public static final int MESSAGE_TYPE_INBOX = 1;
    public static final int MESSAGE_TYPE_SENT = 2;
    public static final int MESSAGE_UNREAD = 0;
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_UNSEEN = 0;
    public static final int MESSAGE_SEEN = 1;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Bundle bundle = intent.getExtras();

		if (bundle != null)
		{
			Object[] pdus = (Object[]) bundle.get(BUNDLE_PDU_KEY);
			for (int i = 0; i < pdus.length; ++i)
			{
				/* convert the message from bytes */
				SmsMessage thisMessage = SmsMessage.createFromPdu((byte[])pdus[i]);
				commitMessage(context, thisMessage);
			}
		}
	}
	
	private void commitMessage(Context context, SmsMessage message)
	{
		ContentValues v = new ContentValues();
		v.put(ADDRESS, message.getOriginatingAddress());
		v.put(DATE, message.getTimestampMillis());
		v.put(SEEN, MESSAGE_UNSEEN);
		v.put(TYPE, MESSAGE_TYPE_INBOX);
		v.put(READ, MESSAGE_UNREAD);
		v.put(STATUS, message.getStatus());
		v.put(BODY, message.getMessageBody());
		
		ContentResolver cr = context.getContentResolver();
		cr.insert(Uri.parse(MESAGE_URI), v);
	}

};

