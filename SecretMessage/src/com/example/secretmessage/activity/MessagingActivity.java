package com.example.secretmessage.activity;


import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.example.secretmessage.R;
import com.example.secretmessage.handler.DatabaseHandler;
import com.example.secretmessage.handler.EncryptionHandler;
import com.example.secretmessage.handler.SmsReceiverHandler;
import com.example.secretmessage.pojo.Contact;
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
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


public class MessagingActivity extends Activity
{
	static final String TAG = MessagingActivity.class.getSimpleName();

	SQLiteDatabase db;

	EncryptionHandler encrypt;

	Button button_SendMessage;
	EditText text_PhoneNumber;
	EditText text_Message;
	ListView messages;

	String history = "";
	SimpleAdapter adapter;
	List<HashMap<String, String>> hashList;
	//ContactManager myContacts = new ContactManager(this);

	static final String PREFS_NAME = "preferences";	
	Uri SMS_URI = Uri.parse("content://sms/inbox");

	SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		prefs = getSharedPreferences("preferences", 0);
		encrypt = EncryptionHandler.getInstance(prefs.getString("auth", null));

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_messaging);
		db = new DatabaseHandler(this).getWritableDatabase();

		Intent intent = getIntent();
		final String recipient = intent.getStringExtra("targetAddress");
		String recipientName = intent.getStringExtra("targetName");

		button_SendMessage = (Button)findViewById(R.id.button_SendMessage);
		text_PhoneNumber = (EditText)findViewById(R.id.text_PhoneNumber);
		text_Message = (EditText)findViewById(R.id.text_Message);
		messages = (ListView)findViewById(R.id.listView_MessageHistory);

		messages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		messages.setStackFromBottom(true);

		updateMessageHistory(recipient);

		String[] from = {"date", "body"};
		int[] to = {R.id.history_date, R.id.history_body};
		adapter = new SimpleAdapter(getBaseContext(), hashList, R.layout.listview_history, from, to);
		messages.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		text_PhoneNumber.setText("<" + recipientName + ">" + "[" + recipient + "]");

		button_SendMessage.setOnClickListener(new View.OnClickListener() 
		{

			public void onClick(View v) 
			{                
				String message = text_Message.getText().toString();    
				if (recipient.length() > 0 && message.length() > 0)
				{
					sendMessage(recipient, message);                
				}
				else			// Display error message; there was an empty field
				{
					Toast.makeText(getBaseContext(), "Either the phone number or message field was left blank.", Toast.LENGTH_SHORT).show();
				}

				text_Message.setText("");
			}
		});        
	}

	void addContact(Contact contact) {	

		ContentValues values = new ContentValues();
		values.put(DatabaseHandler.KEY_NAME, contact.getName()); //Contact Name
		values.put(DatabaseHandler.KEY_PH_NO, contact.getPhoneNumber()); //Contact phone
		values.put(DatabaseHandler.KEY_MSG, contact.getMessage());
		db.insert(DatabaseHandler.TABLE_CONTACTS, null, values);

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		Intent intent = getIntent();
		String recipient = intent.getStringExtra("targetAddress");
		updateMessageHistory(recipient);
	}

	private void sendMessage(String targetNumber, String targetMessage)
	{
		/* Create intent to be exec after sending message, sends us back to 
		 * baseActivity  
		 */
		SmsManager messageManager = SmsManager.getDefault();
		messageManager.sendTextMessage(targetNumber, null, targetMessage, null, null);

		// Commit message to sent
		ContentValues v = new ContentValues();
		v.put(SmsReceiverHandler.ADDRESS, targetNumber);
		v.put(SmsReceiverHandler.BODY, targetMessage); 
		getApplicationContext().getContentResolver().insert(Uri.parse("content://sms/sent"), v);
	}

	public void openSettingsView(View view)
	{
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	public void openBaseActivity(View view)
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
		Cursor c = cr.query(SMS_URI, null, null, null, null);
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
						bodies[count++] = CryptoUtil.encrypt(bodies[count], "pass");
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(getBaseContext(), "Not working", Toast.LENGTH_LONG).show();
					}
				}
			} while (c.moveToNext() && count < 256);
		}

		c.close();

		Cursor cc = cr.query(Uri.parse("content://sms/sent"), null, null, null, null);
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
	public static final String BUNDLE_PDU_KEY = "pdus";

	/* Grab message from bundle and handle it */
	public void onReceive(Context context, Intent intent)
	{
		Bundle bundle = intent.getExtras();
		SmsMessage[] messages = null;
		String body = "";

		if (bundle != null)
		{ 	/* Parse recieved message and push to database */
			Object[] pdus = (Object[]) bundle.get(BUNDLE_PDU_KEY);
			messages = new SmsMessage[pdus.length];

			for (int i = 0; i < pdus.length; i++)
			{
				messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				body += messages[i].getOriginatingAddress() + " : " + messages[i].getMessageBody() + "\n";
				//commitMessage(cr, message);
			}

			Toast.makeText(context, body, Toast.LENGTH_LONG).show();
		}
	}

	   @Override
	   public void onPause() {
	  	db.close(); // Closing database connection
	  }   

};