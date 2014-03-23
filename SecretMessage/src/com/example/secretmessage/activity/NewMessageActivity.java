package com.example.secretmessage.activity;

import com.example.secretmessage.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;


public class NewMessageActivity extends Activity
{
	static final String TAG = NewMessageActivity.class.getSimpleName();

	EditText phoneNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_newmessage);
		phoneNumber = (EditText)findViewById(R.id.edittext_newmessage_recipient);
	}

	public void sendNewMessage(View view)
	{
		String recipient = phoneNumber.getText().toString();
		Intent intent = new Intent(this, BaseActivity.class);
		intent.putExtra("recipient", recipient);
		startActivity(intent);
	}

};
