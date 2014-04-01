package com.example.secretmessage.handler;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.example.secretmessage.activity.MainActivity;
import com.example.secretmessage.activity.MessagingService;
import com.example.secretmessage.pojo.HandshakeStatus;

public class SmsReceiverHandler extends BroadcastReceiver
{
	public static final String TAG = SmsReceiverHandler.class.getSimpleName();
	public static final String timeStamp = "timeStamp";
	public static final String address = "address";
	public static final String messageString = "message";
	public static final String messageStatus = "status";
	public static final String messageHeader = "header";
	public static final String messageDir = "messageDir";
	public static final String outgoing = "outgoing";
	public static final String incoming = "incoming";

	// Working variables
	public static final String BUNDLE_PDU_KEY = "pdus";
	
	Intent intent2;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(TAG, "Recieved message");
		Bundle bundle = intent.getExtras();

		if (bundle != null)
		{
			Object[] pdus = (Object[]) bundle.get(BUNDLE_PDU_KEY);
			for (int i = 0; i < pdus.length; ++i)
			{
				/* convert the message from bytes */
				SmsMessage thisMessage = SmsMessage.createFromPdu((byte[])pdus[i]);
				handleMessage(thisMessage, context);
			}
		}
	}

	private void handleMessage(SmsMessage thisMessage, Context context) {

		String senderNum = thisMessage.getOriginatingAddress();
		String message = thisMessage.getDisplayMessageBody();
		Log.i("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);

		// Show Alert
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, 
				"senderNum: "+ senderNum + ", message: " + message
				+ "context " + context.toString(), duration);
		toast.show();

		intent2 = new Intent(context,  MessagingService.class);
		intent2.putExtra(address, senderNum);
		intent2.putExtra(SmsReceiverHandler.messageDir, SmsReceiverHandler.incoming);
		switch(HandshakeStatus.getStatus(message.substring(0, 1).getBytes()[0]))
		{
		case GENERATED:
		case INIT:
		case RECIEVEDPUBLIC:
		case NOTYETENCRYPT:
			intent2.putExtra(messageHeader, message.substring(0,1));
			intent2.putExtra(message, message.substring(1));
			break;
		default:
			Log.d(TAG, "Message not sent throug secret message app.");
			intent2.putExtra(messageHeader, HandshakeStatus.NOTYETENCRYPT.getValue());
			intent2.putExtra(messageString, message);
			break;

		}
		;
		context.startService(intent2);
	}

	

};

