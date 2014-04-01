package com.example.secretmessage.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Interpolator.Result;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.secretmessage.R;
import com.example.secretmessage.handler.ContactHandler;
import com.example.secretmessage.handler.SmsReceiverHandler;
import com.example.secretmessage.utils.StringUtils;

public class BaseActivity extends Activity {
	static final String TAG = BaseActivity.class.getSimpleName();
	Uri MESSAGE_URI = Uri.parse("content://sms/inbox");
	ContactHandler contacts;
	List<String> addresses = new ArrayList<String>();
	ListView messageThreads;
	Button button_Refresh;
	Button button_Settings;
	Button button_NewMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_base);
		
		messageThreads = (ListView)findViewById(R.id.listView_Threads);
		button_Refresh = (Button)findViewById(R.id.button_Refresh);
		button_Settings = (Button)findViewById(R.id.button_Settings);
		button_NewMessage = (Button)findViewById(R.id.button_NewMessage);

		contacts = new ContactHandler(this);
		contacts.getContactsList(this);
		this.updateConversationLists();

		messageThreads.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
			{
				openMessaging(view, position);
			}
		});

		button_Refresh.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{                
				Log.d(TAG, "Refreshing");
				refresh();
			}
		});


		button_Settings.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{                
				goToSettings(v);
			}
		});

		button_NewMessage.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v) 
			{                
				newMessage(v);
			}
		});

	}

	private void updateConversationLists()
	{	
		Log.d(TAG, "Updating conversation lists.");
		addresses.clear();

		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(MESSAGE_URI, null, null, null, null);

		List<HashMap<String,String>> hashList = new ArrayList<HashMap<String,String>>();

		String[] bodies = new String[256];
		String[] names = new String[256];
		String address;
		String name;


		int addressIndex = c.getColumnIndex(MessagingService.ADDRESS);
		int bodyIndex = c.getColumnIndex(MessagingService.BODY);

		int count = 0;
		if(!c.moveToFirst()) return;
		do
		{
			address = c.getString(addressIndex);
			address = StringUtils.removeSpecialCharacters(address);
			if (!addresses.contains(address))
			{
				bodies[count] = c.getString(bodyIndex);
				name = contacts.getName(address, this);
				names[count++] = name;
				addresses.add(address);
			}
		} while (c.moveToNext() && count < 300);
		for(int i = 0; i < count; i++)
		{
			HashMap<String, String> hm = new HashMap<String,String>();
			hm.put("name", names[i]);
			hm.put("body", bodies[i]);
			hashList.add(hm);
		}

		String[] from = {"name", "body"};
		int[] to = {R.id.date, R.id.body};
		SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), hashList, R.layout.contact_listview_item, from, to);
		messageThreads.setAdapter(adapter);

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		updateConversationLists();
	}

	public void goToSettings(View view)
	{
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	public void refresh()
	{
		Toast.makeText(this, "Renewing contacts list...", Toast.LENGTH_LONG).show();
		contacts.getContactsList(this);
		updateConversationLists();
	}

	static final String reciepientAddress = "recipientAddress";
	static final String reciepientName = "recipientName";
	public void openMessaging(View view, int position)
	{
		String address = addresses.get(position);
		openMessaging(address);
	}
	
	public void openMessaging(String messageAddress)
	{
		Intent intent = new Intent(this, MessagingActivity.class);
		String address = messageAddress;
		String name = contacts.getName(address, this);
		Log.d(TAG, "Opening messaging with " + address + name);
		intent.putExtra(reciepientAddress, address);
		intent.putExtra(reciepientName, name);
		startActivity(intent);
	}

	static final int newMessage = 1;
	public void newMessage(View view)
	{
		Intent intent = new Intent(this, NewMessageActivity.class);
		startActivityForResult(intent, newMessage);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == newMessage) {
			switch(resultCode)
			{
			case RESULT_OK:
				Log.d(TAG, "Got a result from new message");
				openMessaging(data.getStringExtra(NewMessageActivity.newMessagingResult));
				break;
			case RESULT_CANCELED:
				Log.d(TAG, "User cancelled new message");
				break;
			}
		}
	}

};

