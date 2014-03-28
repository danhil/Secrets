package com.example.secretmessage.activity;


import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.example.secretmessage.R;
import com.example.secretmessage.handler.ContactHandler;
import com.example.secretmessage.handler.DatabaseHandler;
import com.example.secretmessage.handler.EncryptionHandler;
import com.example.secretmessage.handler.SmsReceiverHandler;
import com.example.secretmessage.message.Message;
import com.example.secretmessage.pojo.Contact;
import com.example.secretmessage.pojo.HandshakeStatus;
import com.example.secretmessage.utils.CryptoUtil;
import com.example.secretmessage.utils.StringUtils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class MessagingActivity extends Activity
{
	static final String TAG = MessagingActivity.class.getSimpleName();

	SQLiteDatabase db;

	EncryptionHandler encrypt;
	DatabaseHandler dbHandler;

	Button button_SendMessage;
	Button button_init_hs;
	TextView text_encrypt_status;
	EditText text_name_number;
	EditText text_message;
	ListView messages;

	String history = "";
	SimpleAdapter adapter;
	List<HashMap<String, String>> hashList;
	ContactHandler contacts = new ContactHandler(this);
	Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");
	Uri SMS_OUTBOX_URI = Uri.parse("content://sms/sent");

	SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG,"In the oncreate in messageact");
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		prefs = getSharedPreferences(MainActivity.PREFS_NAME, 0);
		encrypt = EncryptionHandler.getInstance(prefs.getString(MainActivity.AUTH, null));
		setContentView(R.layout.activity_messaging);
		dbHandler = new DatabaseHandler(this);
		db = dbHandler.getWritableDatabase();
		/* Get the intent that alunched the messaging activity to get args */
		Intent intent = getIntent();
		final String recipient = intent.getStringExtra(BaseActivity.reciepientAddress);
		String recipientName = intent.getStringExtra(BaseActivity.reciepientName);
		Log.d(TAG, "Recipient " + recipient + " Name " + recipientName);
		button_SendMessage = (Button)findViewById(R.id.button_SendMessage);
		button_init_hs = (Button) findViewById(R.id.button_startHandshake);
		text_encrypt_status = (TextView) findViewById(R.id.text_hs_status);
		text_name_number = (EditText)findViewById(R.id.text_name_number);
		text_message = (EditText)findViewById(R.id.text_message);

		messages = (ListView)findViewById(R.id.listView_MessageHistory);
		messages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		messages.setStackFromBottom(true);

		updateMessageHistory(recipient);
		String[] from = {"date", "body"};
		int[] to = {R.id.history_date, R.id.history_body};
		adapter = new SimpleAdapter(getBaseContext(), hashList, R.layout.listview_history, from, to);
		messages.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		text_name_number.setText(recipientName + " [" + recipient + "] ");

		button_init_hs.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				initHandShake(recipient);
				
			}
		
		});
		
		button_SendMessage.setOnClickListener(new View.OnClickListener() 
		{

			public void onClick(View v) 
			{                
				String message = text_message.getText().toString();    

				if (recipient.length() > 0 && message.length() > 0)
				{
					sendMessage(recipient, message); //Have complete message, send it   
				}
				else
				{
					Toast.makeText(getBaseContext(), "Either the phone number or message field was left blank.", Toast.LENGTH_SHORT).show();
				}

				text_message.setText("");
			}
		});        
	}

	private void sendMessage(String targetNumber, String targetMessage)
	{	
		SmsManager messageManager = SmsManager.getDefault();
		messageManager.sendTextMessage(targetNumber, null, targetMessage, null, null);
		/* Add the sent message to history; */
		ContentValues v = new ContentValues();
		v.put(SmsReceiverHandler.ADDRESS, targetNumber);
		v.put(SmsReceiverHandler.BODY, targetMessage); 
		getApplicationContext().getContentResolver().insert(SMS_OUTBOX_URI, v);
	}
	
	private void initHandShake(String recipient) {
		Log.d(TAG, "Sending public key to " + recipient);
		Message message = new Message('i', encrypt.aProducePublicKey(recipient), recipient);
		SmsManager messageManager = SmsManager.getDefault();
		messageManager.sendTextMessage(recipient, null, message.serialize(prefs.getString(MainActivity.AUTH, null)),null, null);
		dbHandler.updateHsStatus(recipient, HandshakeStatus.SENTPUBLIC);
	}
	
	private void sendPublicKey(String reciepPubkey, String recipient)
	{
		
	}


	@Override
	protected void onResume()
	{
		Log.d(TAG, "In" + this);
		super.onResume();
		Intent intent = getIntent();
		String recipient = intent.getStringExtra(BaseActivity.reciepientAddress);
		updateMessageHistory(recipient);
		HandshakeStatus currentContactStatus = dbHandler.getHSStatus(recipient);
		if(currentContactStatus.equals(HandshakeStatus.INIT))
			button_init_hs.setVisibility(View.VISIBLE);
		if(!currentContactStatus.equals(HandshakeStatus.GENERATED))
			text_encrypt_status.setVisibility(View.VISIBLE);
	}


	public void openSettingsView()
	{
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	public void openBaseActivity()
	{
		Intent intent = new Intent(this, BaseActivity.class);
		startActivity(intent);
	}

	/* Gets recipient messages from database and displays them */
	public void updateMessageHistory(String address)
	{
		String currentAddress;	// For comparing target address against stored address
		long[] dates = new long[256];
		String[] bodies = new String[256];
		int count = 0;
		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(SMS_INBOX_URI, null, null, null, null);
		int addressIndex = c.getColumnIndex(SmsReceiverHandler.ADDRESS);
		int bodyIndex = c.getColumnIndex(SmsReceiverHandler.BODY);
		int dateIndex = c.getColumnIndex(SmsReceiverHandler.DATE);

		Deque<HashMap<String, String>> messageStack = new ArrayDeque<HashMap<String, String>>();

		hashList = new ArrayList<HashMap<String,String>>();

		SimpleDateFormat formatter = new SimpleDateFormat("hh:mma, MMM dd", Locale.US);
		String formattedDate = "";

		if(c.moveToFirst())
		{
			do
			{
				currentAddress = StringUtils.removeSpecialCharacters(c.getString(addressIndex));
				if (currentAddress.equals(address))
				{
					dates[count] = c.getLong(dateIndex);				
					bodies[count] = c.getString(bodyIndex);
					try {
						/* This is the message text from the other part */
						String messageData = bodies[count];
						Log.i(TAG, "Data " + messageData);

						bodies[count++] = CryptoUtil.encrypt(messageData, "pass");
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(getBaseContext(), "Not working", Toast.LENGTH_LONG).show();
					}
				}
			} while (c.moveToNext() && count < 100);
		}

		c.close();

		Cursor cc = cr.query(SMS_OUTBOX_URI, null, null, null, null);
		addressIndex = cc.getColumnIndex(SmsReceiverHandler.ADDRESS);
		bodyIndex = cc.getColumnIndex(SmsReceiverHandler.BODY);
		dateIndex = cc.getColumnIndex(SmsReceiverHandler.DATE);
		int sentCount = 0;
		long[] sentDates = new long[256];
		String[] sentBodies = new String[256];
		if (cc.moveToFirst())
		{
			do
			{
				currentAddress = StringUtils.removeSpecialCharacters(cc.getString(addressIndex));

				if (currentAddress.equals(address))
				{
					sentDates[sentCount] = cc.getLong(dateIndex);
					sentBodies[sentCount++] = "ME: " + cc.getString(bodyIndex);
				}
			} while (cc.moveToNext() && sentCount < 256);
		}

		int index, myIndex;
		index = myIndex = 0;
		while (index < count || myIndex < sentCount)
		{
			HashMap<String, String> hm = new HashMap<String, String>();
			if (index < count && !(myIndex < sentCount))
			{
				hm.put("body", bodies[index]);
				formattedDate = formatter.format(dates[index]);
				hm.put("date", formattedDate);
				index++;
				messageStack.push(hm);
			}
			else if (myIndex < sentCount && !(index < count))
			{
				hm.put("body", sentBodies[myIndex]);
				formattedDate = formatter.format(sentDates[myIndex]);
				hm.put("date", formattedDate);
				myIndex++;
				messageStack.push(hm);
			}
			else if (myIndex < sentCount && index < count)
			{
				if (dates[index] > sentDates[myIndex])
				{
					hm.put("body", bodies[index]);
					formattedDate = formatter.format(dates[index]);
					hm.put("date", formattedDate);
					index++;
					messageStack.push(hm);
				}
				else
				{
					hm.put("body", sentBodies[myIndex]);
					formattedDate = formatter.format(sentDates[myIndex]);
					hm.put("date", formattedDate);
					myIndex++;
					messageStack.push(hm);
				}
			}
		}

		while (!messageStack.isEmpty()) hashList.add(messageStack.pop());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		db.close(); // Closing database connection
	}   

};