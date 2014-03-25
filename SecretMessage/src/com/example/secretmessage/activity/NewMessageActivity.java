package com.example.secretmessage.activity;

import com.example.secretmessage.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;


public class NewMessageActivity extends Activity
{
	static final String TAG = NewMessageActivity.class.getSimpleName();
	static final String newMessagingResult = "newMessagingResult";
	EditText phoneNumber;
	Button sendButton;
	Button cancelButton;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_newmessage);
		phoneNumber = (EditText)findViewById(R.id.edittext_newmessage_recipient);
		sendButton = (Button) findViewById(R.id.button_auth);
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendNewMessage(v);
			}
		});
		cancelButton = (Button) findViewById(R.id.button_cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelNewMessage(v);
			}
		});
		
	}

	public void sendNewMessage(View view)
	{  	
		String recipient = phoneNumber.getText().toString();
		Intent returnIntent = new Intent();
		returnIntent.putExtra(newMessagingResult, recipient);
		setResult(RESULT_OK,returnIntent);     
		finish();
	}
	
	private void cancelNewMessage(View view)
	{  	
		Intent returnIntent = new Intent();
		setResult(RESULT_CANCELED,returnIntent);     
		finish();
	}

};
