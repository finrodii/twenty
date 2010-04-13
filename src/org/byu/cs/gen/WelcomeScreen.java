package org.byu.cs.gen;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.byu.cs.gen.global.HttpInterface;
import org.byu.cs.gen.global.Settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * This is the screen that is diaplyed after a successful login.  It is launched by
 * going to the special URL twentymingen-app:/// (as defined in AndroidManifest.xml)
 * @author Scott Slaugh
 *
 */
public class WelcomeScreen extends Activity {
	
	private Button logout, search;
	private TextView welcomeText;
	
	private Intent mainScreenIntent;
	private Intent searchIntent;
	
	private final Context context = this;

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
				//Create a new thread to handle the logout task
				new LogoutTask().execute(new String[0]);
			}
		});
        
        search = (Button)findViewById(R.id.searchbutton);
        searchIntent = new Intent(this, SearchResults.class);
        search.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Just launch the activity that displays search results.
				startActivity(searchIntent);
			}
		});
        
        //Disable the logout button while we verify the login
        logout.setEnabled(false);
        
        //Create a thread to verify the login to prevent UI lock
        new VerifyLoginTask().execute(new String[0]);
    }
    
    private class VerifyLoginTask extends AsyncTask<String,String,String> {
    	
    	private ProgressDialog loginDialog;
    	
    	@Override
    	protected void onPreExecute() {
    		//Show an indeterminate progress dialog while preparing to login.
    		loginDialog = ProgressDialog.show(context, "", "Completing login process . . .");
    	}

		@Override
		protected String doInBackground(String... params) {
			try {
	        	//Call the verifylogin endpoint in order to complete the login.
				HttpResponse response = HttpInterface.getInstance().executeGet("login/verifylogin.json?oauthtok=" + Settings.getInstance().getSessionToken());
				String result = HttpInterface.getResponseBody(response);
				Map<String,Object> parseResult = HttpInterface.parseJSON(result);
				
				//Make sure we were successful
				boolean success = (Boolean)parseResult.get("success");
				
				if (success) {
					//Get the final session token and username
					String finalToken = parseResult.get("sessionid").toString();
					String username = parseResult.get("username").toString();
					
					Settings.getInstance().setSessionToken(finalToken);
					Settings.getInstance().setUserName(username);
					
					return ("Welcome to 20 Minute Genealogist, " + Settings.getInstance().getUserName() + "!");
				}
				else {
					return null;
				}
				
			} catch (Exception e) {
				Log.e("Error", "Problem with HttpClient!", e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			loginDialog.dismiss();
			
			if (result == null) {
				welcomeText.setText("An error occurred while trying to login to the system!");
				logout.setEnabled(false);
			}
			else {
				welcomeText.setText(result);
				logout.setEnabled(true);
			}
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
