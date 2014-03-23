package com.example.secretmessage.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
	ListView threads;
	Button button_Refresh;
	Button button_Settings;
	Button button_NewMessage;



	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_base);

		threads = (ListView)findViewById(R.id.listView_Threads);
		button_Refresh = (Button)findViewById(R.id.button_Refresh);
		button_Settings = (Button)findViewById(R.id.button_Settings);
		button_NewMessage = (Button)findViewById(R.id.button_NewMessage);

		contacts = new ContactHandler(this);
		contacts.getContactsList(this);
		this.updateConversationLists();

		threads.setOnItemClickListener(new OnItemClickListener()
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
		addresses.clear();

		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(MESSAGE_URI, null, null, null, null);

		List<HashMap<String,String>> hashList = new ArrayList<HashMap<String,String>>();

		String[] bodies = new String[256];
		String[] names = new String[256];
		String address;
		String name;


		int addressIndex = c.getColumnIndex(SmsReceiverHandler.ADDRESS);
		int bodyIndex = c.getColumnIndex(SmsReceiverHandler.BODY);

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
		} while (c.moveToNext() && count < 256);
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
		threads.setAdapter(adapter);

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

	public void openMessaging(View view, int position)
	{
		Intent intent = new Intent(this, MessagingActivity.class);

		String address = addresses.get(position);
		String name = contacts.getName(address, this);
		intent.putExtra("targetAddress", address);
		intent.putExtra("targetName", name);

		startActivity(intent);
	}

	public void newMessage(View view)
	{
		Intent intent = new Intent(this, NewMessageActivity.class);
		startActivity(intent);
	}

};

