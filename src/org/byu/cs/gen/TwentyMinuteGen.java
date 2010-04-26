package org.byu.cs.gen;


import java.util.Map;

import org.apache.http.HttpResponse;
import org.byu.cs.gen.global.HttpInterface;
import org.byu.cs.gen.global.Settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

/**
 * This is the main activity of the Twenty Minute Genealogist application.
 * Currently it only initiates the login sequence.
 * @author Scott Slaugh
 *
 */
public class TwentyMinuteGen extends Activity {
	
	private Button loginButton;
	private final Context context = this;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        loginButton = (Button)findViewById(R.id.loginbutton);
        //Create a listener for when the Login button is clicked.
        loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Create a new thread to handle the login so that the UI doesn't freeze.
				new LoginTask().execute(new String[0]);
			}
		});
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.genmenu, menu);
        return true;
    }
    
    /**
     * This thread class handles the initial login process.
     * @author Scott Slaugh
     *
     */
    private class LoginTask extends AsyncTask<String,String,Intent> {
    	
    	private ProgressDialog loginDialog;
    	
    	@Override
    	protected void onPreExecute() {
    		//Show an indeterminate progress dialog while preparing to login.
    		loginDialog = ProgressDialog.show(context, "", "Logging in . . .");
    	}

		@SuppressWarnings("unchecked")
		@Override
		protected Intent doInBackground(String... params) {
			
			try {
				//Call the login endpoint, passing a URL that will launch the WelcomeScreen
				//activity as the callback URL
				HttpResponse response = HttpInterface.getInstance().executeGet("login/login.json?callback_url=twentymingen-app:///");
				String result = HttpInterface.getResponseBody(response);
				Map<String,Object> parseResult = (Map<String,Object>)HttpInterface.parseJSON(result);
				
				//Grab the oauthtok and login URL values from the result
				String token = parseResult.get("oauthtok").toString();
				String url = parseResult.get("loginurl").toString();
				
				//Save the session token
				Settings.getInstance().setSessionToken(token);
				
				//Create an intent that will launch the web browser to view the login URL
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				return i;
				
			} catch (Exception e) {
				Log.e("Error", "Problem with HttpClient!", e);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Intent result) {
			//Close the progress dialog.
			loginDialog.dismiss();
			//Launch the web browser.
			startActivity(result);
		}
    	
    }
}