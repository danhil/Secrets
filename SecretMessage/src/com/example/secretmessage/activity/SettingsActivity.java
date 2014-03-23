package com.example.secretmessage.activity;

import com.example.secretmessage.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SettingsActivity extends Activity
{
	static final String TAG = SettingsActivity.class.getSimpleName();

	// Buttons and fields to be instantiated
	Button button_clearHistory;
	RadioGroup radioGroup_authType;
	
	// File names
	static final String PREFS_NAME = "preferences";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.activity_settings);
        
        button_clearHistory = (Button)findViewById(R.id.button_clearHistory);
        radioGroup_authType = (RadioGroup)findViewById(R.id.radioGroup_AuthType);
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String radioSelection = settings.getString("authType", "");
        if (radioSelection.equals("password"))
        	radioGroup_authType.check(R.id.radio_AuthType_Pass);
        
        button_clearHistory.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {                
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("messageHistory", "");

                editor.commit();
            }
        });
    }
	
	public void processRadioClick(View view)
	{
		boolean checked = ((RadioButton)view).isChecked();	// Is the radio button checked?
		
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
		
		switch(view.getId())
		{
		case R.id.radio_AuthType_None:
			if (checked)
			{
				editor.putString("authType", "none");		// User doesn't want any authentication
				editor.commit();
			}
			break;
		case R.id.radio_AuthType_Pass:
			if (checked)
			{
				editor.putString("authType", "password");	// User wants password authentication
				editor.commit();
			}
			break;
		default:
			Toast.makeText(getBaseContext(), "Form Error", Toast.LENGTH_SHORT).show();
			break;
		}
	}
	
	public void openChangePinPass(View view)
	{
		Intent intent = new Intent(this, ChangePasswordActivity.class);
        startActivity(intent);	// Launch
	}
	
};