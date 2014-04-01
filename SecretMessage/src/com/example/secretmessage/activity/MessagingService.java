package com.example.secretmessage.activity;

import com.example.secretmessage.handler.DatabaseHandler;
import com.example.secretmessage.handler.EncryptionHandler;
import com.example.secretmessage.handler.SmsReceiverHandler;
import com.example.secretmessage.message.Message;
import com.example.secretmessage.message.OutgoingMessage;
import com.example.secretmessage.pojo.HandshakeStatus;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony.Sms;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Toast;

public class MessagingService extends IntentService {

	static final String TAG = MessagingService.class.getName();
	private Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");
	private Uri SMS_OUTBOX_URI = Uri.parse("content://sms/sent");
	private SQLiteDatabase db;

	private EncryptionHandler encrypt;
	private DatabaseHandler dbHandler;
	private SharedPreferences prefs;

	public MessagingService(String name) {
		super(name);
	}

	public MessagingService() {
		super("MessagingService");
	}

	@Override
	public void onCreate()
	{
		Log.d(TAG, "In onCreate()");
		super.onCreate();
		init();
	}

	private void init()
	{
		prefs = getSharedPreferences(MainActivity.PREFS_NAME, 0);
		encrypt = EncryptionHandler.getInstance(prefs.getString(MainActivity.AUTH, null));
		dbHandler = new DatabaseHandler(this);
		db = dbHandler.getWritableDatabase();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Toast toast = Toast.makeText(this, "Start command in " + this, startId);
		toast.show();
		Log.d(TAG, "In start command");
		return super.onStartCommand(intent,flags,startId);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "In handleIntent");
		String dir = intent.getStringExtra(SmsReceiverHandler.messageDir);
		if(dir.equals(SmsReceiverHandler.outgoing))
			sendMessage(intent.getStringExtra(SmsReceiverHandler.address),
					intent.getStringExtra(SmsReceiverHandler.messageString));
		if(dir.equals(SmsReceiverHandler.incoming))
					handleIncomingIntent(intent);
		
		
	}

	private void handleIncomingIntent(Intent intent) {
		byte header = intent.getStringExtra(SmsReceiverHandler.messageHeader).getBytes()[0];
		String origAddress = intent.getStringExtra(SmsReceiverHandler.address);
		String message = intent.getStringExtra(SmsReceiverHandler.messageString);
		String time = intent.getStringExtra(SmsReceiverHandler.timeStamp);
		String status = intent.getStringExtra(SmsReceiverHandler.messageStatus);
		if(HandshakeStatus.getStatus(header) != HandshakeStatus.NOTYETENCRYPT)
		{
			handleHandShake(HandshakeStatus.getStatus(header), origAddress, message);
		} else
		{
			commitMessage(time, origAddress, status, message);
		}
		
	}

	private void handleHandShake(HandshakeStatus valueOf, String senderNbr,
			String message) {
		// TODO Auto-generated method stub
		
	}

	private void initHandShake(String recipient) {
		Log.d(TAG, "Sending public key to " + recipient);
		Message message = new OutgoingMessage(HandshakeStatus.INIT.getValue(), encrypt.aProducePublicKey(recipient), recipient);
		SmsManager messageManager = SmsManager.getDefault();
		messageManager.sendTextMessage(recipient, null, message.serialize(prefs.getString(MainActivity.AUTH, null)),null, null);
		dbHandler.updateHsStatus(recipient, HandshakeStatus.SENTPUBLIC);	
	}

	private void handlePublicKey(String recipient, String publicKey)
	{
		byte[] pubKey = publicKey.getBytes();
		Log.d(TAG, "Got public key, sending public key to " + recipient);
		Message message = new OutgoingMessage(HandshakeStatus.SENTPUBLIC.getValue(), encrypt.bProducePublicKeyAndSecretKey(pubKey, recipient, prefs.getString(MainActivity.AUTH, null)), recipient);
		SmsManager messageManager = SmsManager.getDefault();
		messageManager.sendTextMessage(recipient, null, message.serialize(prefs.getString(MainActivity.AUTH, null)),null, null);
		dbHandler.updateHsStatus(recipient, HandshakeStatus.GENERATED);	

	}
	
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
	private void commitMessage(String time, String origAddress,
			String status, String message)
	{
		ContentValues v = new ContentValues();
		v.put(ADDRESS, origAddress);
		v.put(DATE, time);
		v.put(SEEN, MESSAGE_UNSEEN);
		v.put(TYPE, MESSAGE_TYPE_INBOX);
		v.put(READ, MESSAGE_UNREAD);
		v.put(STATUS, status);
		v.put(BODY, message);

		ContentResolver cr = getApplicationContext().getContentResolver();
		cr.insert(Uri.parse(MESAGE_URI), v);
	}


	private void sendMessage(String targetNumber, String targetMessage)
	{	
		SmsManager messageManager = SmsManager.getDefault();
		messageManager.sendTextMessage(targetNumber, null, targetMessage, null, null);
		/* Add the sent message to history; */
		ContentValues v = new ContentValues();
		v.put(MessagingService.ADDRESS, targetNumber);
		v.put(MessagingService.BODY, targetMessage); 
		getApplicationContext().getContentResolver().insert(SMS_OUTBOX_URI, v);
	}
	
	@Override
	public void onDestroy()
	{
		db.close();
	}

}
