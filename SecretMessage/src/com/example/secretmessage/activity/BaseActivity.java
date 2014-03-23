package com.example.secretmessage.activity;

import android.app.Activity;
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

public class BaseActivity extends Activity {
static final String TAG = BaseActivity.class.getSimpleName();

List<String> seenAddresses = new ArrayList<String>();
ContactHandler myContacts;
Uri SMS_URI = Uri.parse("content://sms/inbox");

ListView threads;
Button button_Refresh;

@Override
protected void onCreate(Bundle savedInstanceState)
{
super.onCreate(savedInstanceState);

this.requestWindowFeature(Window.FEATURE_NO_TITLE);

setContentView(R.layout.activity_base);

threads = (ListView)findViewById(R.id.listView_Threads);
button_Refresh = (Button)findViewById(R.id.button_Refresh);

}
};

