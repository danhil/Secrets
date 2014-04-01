package com.example.secretmessage.activity;

import com.example.secretmessage.R;
import com.example.secretmessage.R.layout;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {

	// File names and constants
	static final String TAG = MainActivity.class.getSimpleName();
	public static final String PREFS_NAME = "preferences";
	static final String AUTH = "auth";
	static final String AUTHTYPE = "authType";
	static final String NO_DATA = "nodata";
	static final String AUTH_PASS = "password";
	static final String SUDOAUTH = "admin";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toast.makeText(getBaseContext(), "1", Toast.LENGTH_SHORT);

		// Open a shared preferences editor and grab user's stored authentication type
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String storedAuthType = settings.getString(AUTHTYPE, NO_DATA);
		SharedPreferences.Editor editor = settings.edit();

		// Decide which window to open based on the stored authentication method
		if (storedAuthType.equals(NO_DATA))
		{
			editor.putString(AUTHTYPE, AUTH_PASS);
			editor.commit();
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
		}
		else if (storedAuthType.equals(AUTH_PASS))
		{
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
		}
		else
		{
			Toast.makeText(getBaseContext(), "Startup error.", Toast.LENGTH_LONG).show();
		}
	}

};
