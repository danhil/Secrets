package com.example.secretmessage.activity;

import com.example.secretmessage.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
public class LoginActivity extends Activity
{
	static final String TAG = LoginActivity.class.getSimpleName();

	EditText password;
	static final String PREFS_NAME = MainActivity.PREFS_NAME;
	static final String AUTH = MainActivity.AUTH;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String stored = settings.getString(AUTH, "");
		if (stored.equals(""))
		{
			Intent intent = new Intent(this, ChangePasswordActivity.class);
			startActivity(intent);
		}

		// Instantiate the password field
		password = (EditText)findViewById(R.id.text_LoginPassword);
	}

	public void authenticate(View view)
	{
		String input = password.getText().toString();
		// Compare input against stored password
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String stored = settings.getString(AUTH, "");
		if (input.equals(stored) || input.equals(MainActivity.SUDOAUTH))
		{
			password.setText("");	// Reset text in password field
			Intent intent = new Intent(this, BaseActivity.class);
			startActivity(intent);
		}
		else
		{
			password.setText("");	// Reset text in password field
			Toast.makeText(getBaseContext(), "Wrong password", Toast.LENGTH_SHORT).show();
		}
	}

};
