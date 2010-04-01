package org.byu.cs.gen;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.byu.cs.gen.global.HttpInterface;
import org.byu.cs.gen.global.Settings;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WelcomeScreen extends Activity {
	
	private Button logout, search;
	private TextView welcomeText;
	
	private Intent mainScreenIntent;
	private Intent searchIntent;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        
        mainScreenIntent = new Intent(this, TwentyMinuteGen.class);
        
        welcomeText = (TextView)findViewById(R.id.loggedinlabel);
        logout = (Button)findViewById(R.id.logoutbutton);
        logout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new LogoutTask().execute(new String[0]);
			}
		});
        
        search = (Button)findViewById(R.id.searchbutton);
        searchIntent = new Intent(this, SearchResults.class);
        search.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(searchIntent);
			}
		});
        
        try {
			HttpResponse response = HttpInterface.getInstance().executeGet("login/verifylogin.json?oauthtok=" + Settings.getInstance().getSessionToken());
			String result = HttpInterface.getResponseBody(response);
			Map<String,Object> parseResult = HttpInterface.parseJSON(result);
			
			boolean success = (Boolean)parseResult.get("success");
			
			if (success) {
				String finalToken = parseResult.get("sessionid").toString();
				String username = parseResult.get("username").toString();
				
				Settings.getInstance().setSessionToken(finalToken);
				Settings.getInstance().setUserName(username);
				
				welcomeText.setText("Welcome to 20 Minute Genealogist, " + Settings.getInstance().getUserName() + "!");
			}
			else {
				welcomeText.setText("An error occurred while trying to login to the system!");
				logout.setEnabled(false);
			}
			
		} catch (Exception e) {
			Log.e("Error", "Problem with HttpClient!", e);
		}
    }
    
    private class LogoutTask extends AsyncTask<String,String,String> {

		@Override
		protected String doInBackground(String... params) {
			HttpResponse response;
			try {
				response = HttpInterface.getInstance().executeGet("login/logout.json");
				String result = HttpInterface.getResponseBody(response);
				Map<String,Object> parseResult = HttpInterface.parseJSON(result);
				
				boolean success = (Boolean)parseResult.get("success");
				
				if (success) {
					startActivity(mainScreenIntent);
				}
				else {
					Log.e("Logout", "An error occurred while logging out!");
				}
				
			} catch (Exception e) {
				Log.e("Error", "Problem with HttpClient!", e);
			}
			
			return null;
		}
    	
    }
	
}
